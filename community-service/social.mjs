export const EMOTES=['lean','moonwalk','backflip','ironman','ratdance','worm','sit','lay','dab','wave','animerun','tpose','clap','ascend','griddy','o7'];
export const COSMETICS={angelwings:'wings',demonwings:'wings',fairywings:'wings',butterflywings:'wings',demonhorns:'head',angelhalo:'head',purplehalo:'head',greenhalo:'head',bluehalo:'head',katana:'hip',blackbandana:'face',whitebandana:'face',basicbandana:'face',greatsword:'back',basiccap:'head',oceancape:'back',lightningcape:'back',blossomcape:'back',nethercape:'back',basiccape:'back'};
const json=(v,status=200)=>Response.json(v,{status,headers:{'Cache-Control':'no-store'}});
const uuid=x=>typeof x==='string'&&/^[a-f0-9]{32}$/.test(x);
const pair=(a,b)=>[a,b].sort();
export class Social {
  constructor(hub){this.hub=hub;this.db=hub.db;
    for(const sql of [
      'CREATE TABLE IF NOT EXISTS social_users (uuid TEXT PRIMARY KEY,name TEXT)',
      'CREATE TABLE IF NOT EXISTS friends (a TEXT,b TEXT,requester TEXT,accepted INTEGER,updated INTEGER,PRIMARY KEY(a,b))',
      'CREATE TABLE IF NOT EXISTS blocks (owner TEXT,target TEXT,PRIMARY KEY(owner,target))',
      'CREATE TABLE IF NOT EXISTS messages (id INTEGER PRIMARY KEY AUTOINCREMENT,sender TEXT,recipient TEXT,text TEXT,created INTEGER,nonce TEXT,UNIQUE(sender,nonce))',
      'CREATE INDEX IF NOT EXISTS message_thread ON messages(sender,recipient,id)',
      'CREATE TABLE IF NOT EXISTS looks (uuid TEXT PRIMARY KEY,cosmetics TEXT,emote TEXT,started INTEGER,until INTEGER)',
      'CREATE TABLE IF NOT EXISTS social_presence (uuid TEXT PRIMARY KEY,until INTEGER)'
      ,'CREATE TABLE IF NOT EXISTS appearance_presence (uuid TEXT PRIMARY KEY,until INTEGER)'
    ])this.db.exec(sql);
    this.cleanup=0;
  }
  rows(sql,...args){return this.db.exec(sql,...args).toArray();}
  blocked(a,b){return this.rows('SELECT owner FROM blocks WHERE (owner=? AND target=?) OR (owner=? AND target=?)',a,b,b,a).length>0;}
  friend(a,b){const [lo,hi]=pair(a,b);return this.rows('SELECT accepted FROM friends WHERE a=? AND b=?',lo,hi)[0]?.accepted===1&&!this.blocked(a,b);}
  async handle(path,body,session,now){
    const me=session.uuid;
    this.db.exec('INSERT INTO social_users VALUES(?,?) ON CONFLICT(uuid) DO UPDATE SET name=excluded.name',me,session.name);
    if(now-this.cleanup>60000){this.cleanup=now;this.db.exec('DELETE FROM messages WHERE created<?',now-30*86400000);this.db.exec('DELETE FROM social_presence WHERE until<?',now);this.db.exec('DELETE FROM appearance_presence WHERE until<?',now);this.db.exec('DELETE FROM friends WHERE accepted=0 AND updated<?',now-30*86400000);}
    if(path==='/v1/social/state'){
      const relations=this.rows('SELECT * FROM friends WHERE a=? OR b=?',me,me).map(r=>{const id=r.a===me?r.b:r.a;return {uuid:id,name:this.rows('SELECT name FROM social_users WHERE uuid=?',id)[0]?.name||id,kind:r.accepted?'friend':r.requester===me?'outgoing':'incoming',online:!!this.rows('SELECT uuid FROM social_presence WHERE uuid=? AND until>?',id,now).length};});
      this.db.exec('INSERT INTO social_presence VALUES(?,?) ON CONFLICT(uuid) DO UPDATE SET until=excluded.until',me,now+90000);
      return json({relations,blocked:this.rows('SELECT target FROM blocks WHERE owner=?',me).map(r=>({uuid:r.target,name:this.rows('SELECT name FROM social_users WHERE uuid=?',r.target)[0]?.name||r.target})),look:JSON.parse(this.rows('SELECT cosmetics FROM looks WHERE uuid=?',me)[0]?.cosmetics||'[]'),free:true});
    }
    if(path==='/v1/social/request'){
      if(typeof body.name!=='string'||!/^[A-Za-z0-9_]{1,16}$/.test(body.name))return json({error:'Enter a Minecraft Java username'},400);
      if(!this.hub.allow('friend:'+me,8,now))return json({error:'Wait before sending more friend requests'},429);
      const response=await this.hub.lookup('profile',body.name);if(response.status!==200)return json({error:'Minecraft player not found or lookup unavailable'},404);
      const target=await response.json();if(!uuid(target.id)||typeof target.name!=='string')return json({error:'Invalid Minecraft profile'},502);
      if(target.id===me||this.blocked(me,target.id))return json({error:'Friend request unavailable'},400);
      const [a,b]=pair(me,target.id);
      if(this.rows('SELECT a FROM friends WHERE a=? AND b=?',a,b).length)return json({error:'Already friends or a request is pending'},409);
      for(const id of [me,target.id])if(this.rows('SELECT count(*) AS n FROM friends WHERE a=? OR b=?',id,id)[0].n>=100)return json({error:'Friend list/request limit reached'},409);
      this.db.exec('INSERT INTO social_users VALUES(?,?) ON CONFLICT(uuid) DO UPDATE SET name=excluded.name',target.id,target.name);
      this.db.exec('INSERT INTO friends VALUES(?,?,?,?,?)',a,b,me,0,now);return json({ok:true});
    }
    if(path==='/v1/social/respond'){
      if(!uuid(body.target)||!['accept','remove','block','unblock'].includes(body.action)||body.target===me)return json({error:'Invalid friend action'},400);
      const [a,b]=pair(me,body.target);const relation=this.rows('SELECT * FROM friends WHERE a=? AND b=?',a,b)[0];
      if(body.action==='accept'){
        if(!relation||relation.accepted||relation.requester===me||this.blocked(me,body.target))return json({error:'No incoming request'},403);
        this.db.exec('UPDATE friends SET accepted=1,updated=? WHERE a=? AND b=?',now,a,b);
      }else if(body.action==='unblock')this.db.exec('DELETE FROM blocks WHERE owner=? AND target=?',me,body.target);
      else{
        if(body.action==='block'){
          if(this.rows('SELECT count(*) AS n FROM blocks WHERE owner=?',me)[0].n>=500)return json({error:'Block list is full'},409);
          this.db.exec('INSERT OR IGNORE INTO blocks VALUES(?,?)',me,body.target);
        }
        this.db.exec('DELETE FROM friends WHERE a=? AND b=?',a,b);
        this.db.exec('DELETE FROM messages WHERE (sender=? AND recipient=?) OR (sender=? AND recipient=?)',me,body.target,body.target,me);
      }
      return json({ok:true});
    }
    if(path==='/v1/social/messages'||path==='/v1/social/send'){
      if(!uuid(body.target)||!this.friend(me,body.target))return json({error:'Only accepted friends can exchange messages'},403);
      if(path.endsWith('/send')){
        if(typeof body.text!=='string'||!body.text.trim()||body.text.length>1000||/[\x00-\x08\x0B-\x1F\x7F\u00A7]/.test(body.text)||typeof body.nonce!=='string'||!/^[a-zA-Z0-9-]{16,64}$/.test(body.nonce))return json({error:'Message must be 1-1000 plain-text characters'},400);
        if(!this.hub.allow('message:'+me,30,now))return json({error:'Please slow down'},429);
        if(this.rows('SELECT count(*) AS n FROM messages WHERE sender=?',me)[0].n>=5000)return json({error:'Message storage limit reached; old messages expire after 30 days'},409);
        this.db.exec('INSERT OR IGNORE INTO messages(sender,recipient,text,created,nonce) VALUES(?,?,?,?,?)',me,body.target,body.text.trim(),now,body.nonce);
      }
      const messages=this.rows('SELECT id,sender,text,created FROM messages WHERE (sender=? AND recipient=?) OR (sender=? AND recipient=?) ORDER BY id DESC LIMIT 100',me,body.target,body.target,me).reverse();return json({messages});
    }
    if(path==='/v1/social/equip'){
      if(!Array.isArray(body.cosmetics)||body.cosmetics.length>5||body.cosmetics.some(id=>typeof id!=='string'||!Object.hasOwn(COSMETICS,id))||new Set(body.cosmetics.map(id=>COSMETICS[id])).size!==body.cosmetics.length)return json({error:'Choose one free cosmetic per slot'},400);
      this.db.exec("INSERT INTO looks VALUES(?,?,'',0,0) ON CONFLICT(uuid) DO UPDATE SET cosmetics=excluded.cosmetics",me,JSON.stringify(body.cosmetics));return json({ok:true});
    }
    if(path==='/v1/social/emote'){
      if(!EMOTES.includes(body.emote)&&body.emote!=='')return json({error:'Unknown emote'},400);
      if(!this.hub.allow('emote:'+me,20,now))return json({error:'Please slow down'},429);
      const until=body.emote?now+(['sit','lay','tpose'].includes(body.emote)?120000:15000):0;
      this.db.exec("INSERT INTO looks VALUES(?,'[]',?,?,?) ON CONFLICT(uuid) DO UPDATE SET emote=excluded.emote,started=excluded.started,until=excluded.until",me,body.emote,now,until);return json({emote:body.emote,started:now,until});
    }
    if(path==='/v1/social/presence'){
      if(!Array.isArray(body.players)||body.players.length>100||!body.players.every(uuid))return json({error:'Invalid player list'},400);
      this.db.exec('INSERT INTO social_presence VALUES(?,?) ON CONFLICT(uuid) DO UPDATE SET until=excluded.until',me,now+15000);
      this.db.exec('INSERT INTO appearance_presence VALUES(?,?) ON CONFLICT(uuid) DO UPDATE SET until=excluded.until',me,now+15000);
      const ids=[...new Set(body.players)],players={};
      if(ids.length)for(const row of this.rows('SELECT l.* FROM looks l JOIN appearance_presence p ON p.uuid=l.uuid WHERE p.until>? AND l.uuid IN ('+ids.map(()=>'?').join(',')+')',now,...ids))players[row.uuid]={cosmetics:JSON.parse(row.cosmetics),emote:row.until>now?row.emote:'',started:row.started,until:row.until};
      return json({players,now});
    }
    if(path==='/v1/social/leave'){this.db.exec('DELETE FROM social_presence WHERE uuid=?',me);this.db.exec('DELETE FROM appearance_presence WHERE uuid=?',me);this.db.exec("UPDATE looks SET emote='',until=0 WHERE uuid=?",me);return json({ok:true});}
    return json({error:'Not found'},404);
  }
}
