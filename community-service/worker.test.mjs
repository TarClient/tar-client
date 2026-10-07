import {test} from 'node:test';
import assert from 'node:assert/strict';
import {DatabaseSync} from 'node:sqlite';
import {Community,OWNER} from './worker.mjs';
const OTHER='aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa';
function setup(){
  const database=new DatabaseSync(':memory:');
  const sql={exec(statement,...args){const prepared=database.prepare(statement);if(/^SELECT/.test(statement))return {toArray:()=>prepared.all(...args)};prepared.run(...args);return {toArray:()=>[]};}};
  let proofProfile={id:OWNER,name:'Tarrecool'};
  const hub=new Community({storage:{sql}},{MOJANG_FETCH:async url=>Response.json(url.includes('hasJoined')?proofProfile:{id:OTHER,name:'OtherPlayer'})});
  const send=(path,body={},token)=>hub.fetch(new Request('https://community.example'+path,{method:'POST',headers:{'Content-Type':'application/json',...(token?{Authorization:'Bearer '+token}:{})},body:JSON.stringify(body)}));
  async function login(id,name){proofProfile={id,name};const proof=await (await send('/v1/challenge',{uuid:id,name})).json();const response=await send('/v1/confirm',{id:proof.id});assert.equal(response.status,200);return (await response.json()).token;}
  return {hub,send,login,setProfile:p=>proofProfile=p,database};
}
test('a username or UUID claim cannot obtain the owner session without Mojang proof',async()=>{
  const s=setup();s.setProfile({id:OTHER,name:'OtherPlayer'});
  const proof=await (await s.send('/v1/challenge',{uuid:OWNER,name:'Tarrecool'})).json();
  assert.equal((await s.send('/v1/confirm',{id:proof.id})).status,401);
  assert.equal((await s.send('/v1/confirm',{id:proof.id})).status,401);
  assert.equal((await s.send('/v1/rank',{name:'OtherPlayer',rank:'admin',uuid:OWNER})).status,401);
});
test('only the owner can grant, change, and revoke a rank; Admin does not grant that permission',async()=>{
  const s=setup(),owner=await s.login(OWNER,'Tarrecool'),other=await s.login(OTHER,'OtherPlayer');
  assert.equal((await s.send('/v1/rank',{name:'OtherPlayer',rank:'partner'},owner)).status,200);
  assert.equal((await s.send('/v1/rank',{name:'OtherPlayer',rank:'admin'},owner)).status,200);
  assert.equal((await s.send('/v1/rank',{name:'OtherPlayer',rank:'mod',uuid:OWNER},other)).status,403);
  assert.equal((await s.send('/v1/ranks',{},other)).status,403);
  assert.equal((await s.send('/v1/rank',{name:'OtherPlayer',rank:'normal'},owner)).status,200);
  assert.deepEqual((await (await s.send('/v1/ranks',{},owner)).json()).ranks,[]);
});
test('badges require recent verified presence; another account cannot claim someone else is online',async()=>{
  const s=setup(),owner=await s.login(OWNER,'Tarrecool'),other=await s.login(OTHER,'OtherPlayer');
  let response=await (await s.send('/v1/heartbeat',{players:[OWNER,OTHER],uuid:OTHER},owner)).json();
  assert.deepEqual(response.players,{[OWNER]:'admin'});
  response=await (await s.send('/v1/heartbeat',{players:[OWNER,OTHER]},other)).json();
  assert.deepEqual(response.players,{[OWNER]:'admin',[OTHER]:'normal'});
  await s.send('/v1/leave',{},other);
  response=await (await s.send('/v1/heartbeat',{players:[OWNER,OTHER]},owner)).json();
  assert.equal(response.players[OTHER],undefined);
  s.database.prepare('UPDATE presence SET expires=0 WHERE uuid=?').run(OTHER);
  assert.equal((await s.send('/v1/heartbeat',{players:Array(101).fill(OTHER)},owner)).status,400);
});
test('tokens are hashed in storage, expire, and cannot be reused as another player',async()=>{
  const s=setup(),token=await s.login(OWNER,'Tarrecool');
  const stored=s.database.prepare('SELECT token FROM sessions').get();assert.notEqual(stored.token,token);
  s.database.prepare('UPDATE sessions SET expires=0').run();
  assert.equal((await s.send('/v1/rank',{name:'OtherPlayer',rank:'admin'},token)).status,401);
});
test('a successful challenge is consumed and cannot create a second session',async()=>{
  const s=setup(),proof=await (await s.send('/v1/challenge',{uuid:OWNER,name:'Tarrecool'})).json();
  const replies=await Promise.all([s.send('/v1/confirm',{id:proof.id}),s.send('/v1/confirm',{id:proof.id})]);
  assert.deepEqual(replies.map(x=>x.status).sort(),[200,401]);
});
