import {test} from 'node:test';
import assert from 'node:assert/strict';
import {DatabaseSync} from 'node:sqlite';
import {Community} from './worker.mjs';
import {EMOTES,COSMETICS} from './social.mjs';
const A='a'.repeat(32),B='b'.repeat(32),C='c'.repeat(32);
function setup(){
 const db=new DatabaseSync(':memory:');const sql={exec(q,...args){const stmt=db.prepare(q);return /^SELECT/.test(q)?{toArray:()=>stmt.all(...args)}:(stmt.run(...args),{toArray:()=>[]});}};
 let profile={id:A,name:'Alice'};const hub=new Community({storage:{sql}},{MOJANG_FETCH:async url=>Response.json(url.includes('hasJoined')?profile:{id:B,name:'Bob'})});
 const send=(path,body={},token)=>hub.fetch(new Request('https://example.test/v1/'+path,{method:'POST',headers:{'Content-Type':'application/json',...(token?{Authorization:'Bearer '+token}:{})},body:JSON.stringify(body)}));
 async function login(uuid,name){profile={id:uuid,name};const proof=await (await send('challenge',{uuid,name})).json();return (await (await send('confirm',{id:proof.id})).json()).token;}
 return {db,hub,send,login};
}
test('friend requests require recipient acceptance before messages, and are private',async()=>{
 const s=setup(),a=await s.login(A,'Alice'),b=await s.login(B,'Bob'),c=await s.login(C,'Cat');
 assert.equal((await s.send('social/request',{name:'Bob'},a)).status,200);
 assert.equal((await s.send('social/respond',{target:B,action:'accept'},a)).status,403);
 assert.equal((await s.send('social/send',{target:B,text:'Hi',nonce:'1234567890123456'},a)).status,403);
 assert.equal((await s.send('social/respond',{target:A,action:'accept'},b)).status,200);
 const send=await s.send('social/send',{target:B,text:'Hi Bob',nonce:'1234567890123456'},a);assert.equal(send.status,200);
 assert.equal((await (await s.send('social/messages',{target:A},b)).json()).messages[0].text,'Hi Bob');
 assert.equal((await s.send('social/messages',{target:A},c)).status,403);
 assert.equal((await (await s.send('social/state',{},c)).json()).relations.length,0);
});
test('blocking removes requests and chat access; only blocker can unblock',async()=>{
 const s=setup(),a=await s.login(A,'Alice'),b=await s.login(B,'Bob');await s.send('social/request',{name:'Bob'},a);await s.send('social/respond',{target:A,action:'accept'},b);
 await s.send('social/send',{target:B,text:'Hello',nonce:'1234567890123456'},a);
 await s.send('social/respond',{target:A,action:'block'},b);
 assert.equal((await s.send('social/messages',{target:B},a)).status,403);
 await s.send('social/respond',{target:B,action:'unblock'},a);
 assert.equal((await s.send('social/request',{name:'Bob'},a)).status,400);
 assert.equal(s.db.prepare('SELECT count(*) n FROM messages').get().n,0);
 await s.send('social/respond',{target:A,action:'unblock'},b);
 assert.equal((await s.send('social/request',{name:'Bob'},a)).status,200);
});
test('message retry is idempotent and text/size validation rejects control formatting',async()=>{
 const s=setup(),a=await s.login(A,'Alice'),b=await s.login(B,'Bob');await s.send('social/request',{name:'Bob'},a);await s.send('social/respond',{target:A,action:'accept'},b);
 for(let i=0;i<2;i++)assert.equal((await s.send('social/send',{target:B,text:'Hi',nonce:'1234567890123456'},a)).status,200);
 assert.equal(s.db.prepare('SELECT count(*) n FROM messages').get().n,1);
 for(const text of ['','x'.repeat(1001),'\u00a7khidden','bad\x00'])assert.equal((await s.send('social/send',{target:B,text,nonce:'other123456789012'},a)).status,400);
});
test('all requested items are free and equipment cannot forge unknown or duplicate slots',async()=>{
 const s=setup(),a=await s.login(A,'Alice');assert.equal(EMOTES.length,16);assert.equal(Object.keys(COSMETICS).length,20);
 for(const id of Object.keys(COSMETICS))assert.equal((await s.send('social/equip',{cosmetics:[id],uuid:B},a)).status,200);
 for(const cosmetics of [['nope'],['angelhalo','bluehalo'],[{}],[['angelwings']]])assert.equal((await s.send('social/equip',{cosmetics},a)).status,400);
 assert.equal(s.db.prepare('SELECT count(*) n FROM looks WHERE uuid=?').get(B).n,0);
 assert.equal((await (await s.send('social/state',{},a)).json()).free,true);
});
test('emotes/presence are authenticated, expire, and leave clears shared state',async()=>{
 const s=setup(),a=await s.login(A,'Alice'),b=await s.login(B,'Bob');await s.send('social/equip',{cosmetics:['angelwings']},a);
 assert.equal((await s.send('social/emote',{emote:'wave'})).status,401);
 await s.send('social/emote',{emote:'wave',uuid:B},a);await s.send('social/presence',{players:[]},a);
 let data=await (await s.send('social/presence',{players:[A]},b)).json();assert.equal(data.players[A].emote,'wave');assert.deepEqual(data.players[A].cosmetics,['angelwings']);
 s.db.prepare('UPDATE looks SET until=0 WHERE uuid=?').run(A);
 data=await (await s.send('social/presence',{players:[A]},b)).json();assert.equal(data.players[A].emote,'');
 await s.send('social/leave',{},a);data=await (await s.send('social/presence',{players:[A]},b)).json();assert.equal(data.players[A],undefined);
});
test('launcher and in-game sessions coexist without logging each other out',async()=>{
 const s=setup(),first=await s.login(A,'Alice'),second=await s.login(A,'Alice');
 assert.equal((await s.send('social/state',{},first)).status,200);assert.equal((await s.send('social/state',{},second)).status,200);
});
test('a block added during username lookup prevents a pending request',async()=>{
 const s=setup(),a=await s.login(A,'Alice');s.hub.env.MOJANG_FETCH=async()=>{s.db.prepare('INSERT INTO blocks VALUES(?,?)').run(B,A);return Response.json({id:B,name:'Bob'});};
 assert.equal((await s.send('social/request',{name:'Bob'},a)).status,400);assert.equal(s.db.prepare('SELECT count(*) n FROM friends').get().n,0);
});
test('opening friends alone never advertises cosmetics, and appearance presence expires',async()=>{
 const s=setup(),a=await s.login(A,'Alice'),b=await s.login(B,'Bob');await s.send('social/equip',{cosmetics:['angelhalo']},a);await s.send('social/state',{},a);
 assert.equal((await (await s.send('social/presence',{players:[A]},b)).json()).players[A],undefined);
 await s.send('social/presence',{players:[]},a);s.db.prepare('UPDATE appearance_presence SET until=0 WHERE uuid=?').run(A);
 assert.equal((await (await s.send('social/presence',{players:[A]},b)).json()).players[A],undefined);
});
test('rate limits bound friend requests and messages, with no duplicate friendship',async()=>{
 const s=setup(),a=await s.login(A,'Alice'),b=await s.login(B,'Bob');
 await s.send('social/request',{name:'Bob'},a);assert.equal((await s.send('social/request',{name:'Bob'},a)).status,409);await s.send('social/respond',{target:A,action:'accept'},b);
 for(let i=0;i<30;i++)assert.equal((await s.send('social/send',{target:B,text:'test',nonce:'message-nonce-'+String(i).padStart(6,'0')},a)).status,200);
 assert.equal((await s.send('social/send',{target:B,text:'test',nonce:'message-limit-123456'},a)).status,429);
 for(let i=0;i<6;i++)await s.send('social/request',{name:'Bob'},a);
 assert.equal((await s.send('social/request',{name:'Bob'},a)).status,429);
});
test('old messages are removed and pending requests expire after 30 days',async()=>{
 const s=setup(),a=await s.login(A,'Alice'),b=await s.login(B,'Bob');await s.send('social/request',{name:'Bob'},a);await s.send('social/respond',{target:A,action:'accept'},b);await s.send('social/send',{target:B,text:'expired',nonce:'1234567890123456'},a);
 s.db.prepare('UPDATE messages SET created=0').run();s.db.prepare('UPDATE friends SET accepted=0,updated=0').run();s.hub.social.cleanup=0;
 await s.send('social/state',{},a);assert.equal(s.db.prepare('SELECT count(*) n FROM messages').get().n,0);assert.equal(s.db.prepare('SELECT count(*) n FROM friends').get().n,0);
});
