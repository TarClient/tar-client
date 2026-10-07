export const OWNER = 'ccb2c06282bc4afb86a71058b176dbef';
const uuid = value => typeof value === 'string' && /^[a-f0-9]{32}$/.test(value);
const username = value => typeof value === 'string' && /^[A-Za-z0-9_]{1,16}$/.test(value);
const random = () => [...crypto.getRandomValues(new Uint8Array(24))].map(b => b.toString(16).padStart(2,'0')).join('');
const sha = async value => [...new Uint8Array(await crypto.subtle.digest('SHA-256',new TextEncoder().encode(value)))].map(b=>b.toString(16).padStart(2,'0')).join('');
const json = (value,status=200) => Response.json(value,{status,headers:{'Cache-Control':'no-store','X-Content-Type-Options':'nosniff'}});
export default {fetch(request,env){return env.COMMUNITY.getByName('tar-community-v1').fetch(request);}};

export class Community {
  constructor(ctx,env){
    this.db=ctx.storage.sql; this.env=env; this.limits=new Map(); this.lastCleanup=0;
    this.db.exec('CREATE TABLE IF NOT EXISTS challenges (id TEXT PRIMARY KEY, uuid TEXT, name TEXT, hash TEXT, expires INTEGER)');
    this.db.exec('CREATE TABLE IF NOT EXISTS sessions (token TEXT PRIMARY KEY, uuid TEXT, name TEXT, expires INTEGER)');
    this.db.exec('CREATE TABLE IF NOT EXISTS presence (uuid TEXT PRIMARY KEY, expires INTEGER)');
    this.db.exec('CREATE TABLE IF NOT EXISTS ranks (uuid TEXT PRIMARY KEY, name TEXT, rank TEXT, updated INTEGER)');
    this.db.exec('CREATE INDEX IF NOT EXISTS session_expiry ON sessions(expires)');
    this.db.exec('CREATE INDEX IF NOT EXISTS challenge_expiry ON challenges(expires)');
  }
  rows(sql,...params){return this.db.exec(sql,...params).toArray();}
  allow(key,max,now){
    if(this.limits.size>=10000){for(const [k,v] of this.limits)if(v.until<=now)this.limits.delete(k);if(this.limits.size>=10000&&!this.limits.has(key))return false;}
    let item=this.limits.get(key);if(!item||item.until<=now)item={count:0,until:now+60000};item.count++;this.limits.set(key,item);return item.count<=max;
  }
  async fetch(request){
    const now=Date.now(),path=new URL(request.url).pathname;
    if(request.method==='GET'&&path==='/health')return json({service:'Tar Client community',version:1});
    if(request.method!=='POST')return json({error:'Method not allowed'},405);
    const ip=request.headers.get('CF-Connecting-IP')||'local';
    if(!this.allow('ip:'+ip,100,now))return json({error:'Please wait before retrying'},429);
    try {
      if(Number(request.headers.get('Content-Length')||0)>8192)return json({error:'Request too large'},413);
      let text='';const reader=request.body?.getReader();let bytes=0;const decoder=new TextDecoder();
      if(reader)try{while(true){const chunk=await reader.read();if(chunk.done)break;bytes+=chunk.value.byteLength;if(bytes>8192){await reader.cancel();return json({error:'Request too large'},413);}text+=decoder.decode(chunk.value,{stream:true});}text+=decoder.decode();}finally{reader.releaseLock();}
      const body=JSON.parse(text||'{}');
      if(!body||Array.isArray(body)||typeof body!=='object')return json({error:'Invalid request'},400);
      if(now-this.lastCleanup>60000){this.lastCleanup=now;this.db.exec('DELETE FROM challenges WHERE expires < ?',now);this.db.exec('DELETE FROM sessions WHERE expires < ?',now);this.db.exec('DELETE FROM presence WHERE expires < ?',now);}
      if(path==='/v1/challenge'){
        if(!uuid(body.uuid)||!username(body.name))return json({error:'Invalid Minecraft identity'},400);
        if(!this.allow('challenge:'+ip,8,now))return json({error:'Please wait before signing in again'},429);
        const id=random(),hash=random().slice(0,40);
        this.db.exec('INSERT INTO challenges VALUES(?,?,?,?,?)',id,body.uuid,body.name,hash,now+60000);
        return json({id,serverId:hash});
      }
      if(path==='/v1/confirm'){
        if(typeof body.id!=='string'||body.id.length!==48)return json({error:'Invalid challenge'},400);
        const proof=this.rows('SELECT * FROM challenges WHERE id=? AND expires>?',body.id,now)[0];
        if(!proof)return json({error:'Challenge expired'},401);
        // Consume before awaiting Mojang: a challenge cannot be replayed by concurrent requests.
        this.db.exec('DELETE FROM challenges WHERE id=?',body.id);
        const lookup=await (this.env.MOJANG_FETCH||fetch)('https://sessionserver.mojang.com/session/minecraft/hasJoined?username='+encodeURIComponent(proof.name)+'&serverId='+encodeURIComponent(proof.hash));
        if(!lookup.ok||lookup.status===204)return json({error:'Minecraft could not verify this account'},401);
        const profile=await lookup.json();
        if(profile.id!==proof.uuid||String(profile.name).toLowerCase()!==proof.name.toLowerCase())return json({error:'Minecraft identity did not match'},401);
        const token=random(),digest=await sha(token);
        this.db.exec('DELETE FROM sessions WHERE uuid=?',proof.uuid);
        this.db.exec('INSERT INTO sessions VALUES(?,?,?,?)',digest,proof.uuid,profile.name,now+21600000);
        return json({token,uuid:proof.uuid,expires:now+21600000,owner:proof.uuid===OWNER});
      }
      const bearer=request.headers.get('Authorization')||'';
      if(!/^Bearer [a-f0-9]{48}$/.test(bearer))return json({error:'Sign in to Minecraft first'},401);
      const session=this.rows('SELECT * FROM sessions WHERE token=? AND expires>?',await sha(bearer.slice(7)),now)[0];
      if(!session)return json({error:'Community session expired'},401);
      if(path==='/v1/heartbeat'){
        if(!Array.isArray(body.players)||body.players.length>100||!body.players.every(uuid))return json({error:'Invalid player list'},400);
        this.db.exec('INSERT INTO presence VALUES(?,?) ON CONFLICT(uuid) DO UPDATE SET expires=excluded.expires',session.uuid,now+120000);
        const result={};
        for(const id of new Set(body.players)){
          if(!this.rows('SELECT uuid FROM presence WHERE uuid=? AND expires>?',id,now).length)continue;
          result[id]=id===OWNER?'admin':this.rows('SELECT rank FROM ranks WHERE uuid=?',id)[0]?.rank||'normal';
        }
        return json({players:result,validUntil:now+120000});
      }
      if(path==='/v1/leave'){this.db.exec('DELETE FROM presence WHERE uuid=?',session.uuid);return json({ok:true});}
      if(path==='/v1/ranks'||path==='/v1/rank'){
        // Rank names never grant management permissions. Only this verified UUID may write.
        if(session.uuid!==OWNER)return json({error:'Only Tarrecool can manage ranks'},403);
        if(path==='/v1/ranks')return json({ranks:this.rows('SELECT uuid,name,rank,updated FROM ranks ORDER BY name')});
        if(!username(body.name)||!['normal','partner','mod','admin'].includes(body.rank))return json({error:'Choose a Minecraft username and valid rank'},400);
        const lookup=await (this.env.MOJANG_FETCH||fetch)('https://api.mojang.com/users/profiles/minecraft/'+encodeURIComponent(body.name));
        if(!lookup.ok||lookup.status===204)return json({error:'Minecraft username not found'},404);
        const target=await lookup.json();if(!uuid(target.id)||!username(target.name))return json({error:'Minecraft profile unavailable'},502);
        if(target.id===OWNER)return json({error:'The owner always has the red badge'},400);
        if(body.rank==='normal')this.db.exec('DELETE FROM ranks WHERE uuid=?',target.id);
        else this.db.exec('INSERT INTO ranks VALUES(?,?,?,?) ON CONFLICT(uuid) DO UPDATE SET name=excluded.name,rank=excluded.rank,updated=excluded.updated',target.id,target.name,body.rank,now);
        return json({uuid:target.id,name:target.name,rank:body.rank});
      }
      return json({error:'Not found'},404);
    } catch(error){return json({error:error instanceof SyntaxError?'Invalid JSON':'Community service unavailable'},error instanceof SyntaxError?400:503);}
  }
}
