import {test} from 'node:test';
import assert from 'node:assert/strict';
import {createVerifier} from './functions/mojang.mjs';
const secret = 'a'.repeat(64), id = 'b'.repeat(32);
const body = {kind:'session',name:'Tarrecool',serverId:'c'.repeat(40)};
const request = (data = body, auth = secret) => new Request('https://verifier.example/.netlify/functions/mojang', {
  method:'POST',headers:{Authorization:'Bearer '+auth},body:JSON.stringify(data)
});
test('only the backend secret can request a lookup; missing configuration fails closed', async () => {
  let calls = 0;
  const fetcher = async () => { calls++; return Response.json({id,name:'Tarrecool'}); };
  assert.equal((await createVerifier({secret,fetcher})(request(body,'d'.repeat(64)))).status,401);
  assert.equal((await createVerifier({secret:undefined,fetcher})(request())).status,503);
  assert.equal(calls,0);
});
test('strict request validation rejects URLs, tokens, malformed identities and oversized bodies', async () => {
  let calls = 0;
  const handler = createVerifier({secret,fetcher:async () => {calls++; throw Error();}});
  for (const data of [null,[],{...body,name:'../admin'},{...body,serverId:'123'},
    {...body,url:'https://attacker.example'},{...body,accessToken:'secret'}, {kind:'other',name:'Tarrecool'}]) {
    assert.equal((await handler(request(data))).status,400);
  }
  assert.equal((await handler(request({...body,extra:'x'.repeat(2000)}))).status,413);
  assert.equal(calls,0);
});
test('lookup destinations are fixed, redirects disabled and response contains only identity', async () => {
  const seen=[];
  const handler=createVerifier({secret,fetcher:async (url,options)=>{
    seen.push(url); assert.equal(options.redirect,'manual'); assert.equal(options.headers.Authorization,undefined);
    return Response.json({id,name:'Tarrecool',properties:[{value:'private upstream metadata'}]});
  }});
  assert.deepEqual(await (await handler(request())).json(),{id,name:'Tarrecool'});
  assert.deepEqual(await (await handler(request({kind:'profile',name:'Tarrecool'}))).json(),{id,name:'Tarrecool'});
  assert.equal(seen[0],'https://sessionserver.mojang.com/session/minecraft/hasJoined?username=Tarrecool&serverId='+body.serverId);
  assert.equal(seen[1],'https://api.mojang.com/users/profiles/minecraft/Tarrecool');
});
test('absent proofs and upstream failures never produce an identity', async () => {
  for (const status of [204,302,403,404,429,500]) {
    const handler=createVerifier({secret,fetcher:async()=>new Response(status===204?null:'untrusted',{status})});
    const response=await handler(request());
    assert.equal(response.status,status===302?502:status);
    assert.ok(!(await response.text()).includes('untrusted'));
  }
});
test('bad Mojang responses and network failures fail closed', async () => {
  for (const profile of [null,{}, {id,name:'SomebodyElse'}, {id:'wrong',name:'Tarrecool'}]) {
    assert.equal((await createVerifier({secret,fetcher:async()=>Response.json(profile)})(request())).status,502);
  }
  assert.equal((await createVerifier({secret,fetcher:async()=>{throw Error('credentials must not appear');}})(request())).status,502);
});
