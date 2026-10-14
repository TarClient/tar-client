import {timingSafeEqual} from 'node:crypto';

const validName = value => typeof value === 'string' && /^[A-Za-z0-9_]{1,16}$/.test(value);
const reply = (value, status = 200) => Response.json(value, {status, headers:{'Cache-Control':'no-store','X-Content-Type-Options':'nosniff'}});

async function boundedText(body, max) {
  if (!body) return '';
  const reader = body.getReader(), decoder = new TextDecoder();
  let bytes = 0, text = '';
  try {
    for (;;) {
      const chunk = await reader.read();
      if (chunk.done) break;
      bytes += chunk.value.byteLength;
      if (bytes > max) { await reader.cancel(); throw new RangeError('Body too large'); }
      text += decoder.decode(chunk.value, {stream:true});
    }
    return text + decoder.decode();
  } finally { reader.releaseLock(); }
}

// Only the maintainer's community backend can invoke these two fixed Mojang lookups.
// This service never accepts a Minecraft access token or an arbitrary destination URL.
export function createVerifier({secret, fetcher = fetch}) {
  return async request => {
    if (request.method !== 'POST') return reply({error:'Method not allowed'},405);
    if (typeof secret !== 'string' || !/^[a-f0-9]{64}$/.test(secret)) return reply({error:'Verifier not configured'},503);
    const supplied = request.headers.get('Authorization') || '';
    const expected = 'Bearer ' + secret;
    if (!/^Bearer [a-f0-9]{64}$/.test(supplied) || !timingSafeEqual(Buffer.from(supplied), Buffer.from(expected))) return reply({error:'Unauthorized'},401);
    let body;
    try { body = JSON.parse(await boundedText(request.body,1024)); }
    catch (error) { return reply({error:'Invalid request'}, error instanceof RangeError ? 413 : 400); }
    if (!body || typeof body !== 'object' || Array.isArray(body) || !validName(body.name)) return reply({error:'Invalid request'},400);
    let url, keys;
    if (body.kind === 'session' && typeof body.serverId === 'string' && /^[a-f0-9]{40}$/.test(body.serverId)) {
      keys = ['kind','name','serverId'];
      url = 'https://sessionserver.mojang.com/session/minecraft/hasJoined?username=' + encodeURIComponent(body.name) + '&serverId=' + body.serverId;
    } else if (body.kind === 'profile') {
      keys = ['kind','name'];
      url = 'https://api.mojang.com/users/profiles/minecraft/' + encodeURIComponent(body.name);
    } else return reply({error:'Invalid request'},400);
    if (Object.keys(body).some(key => !keys.includes(key))) return reply({error:'Unexpected field'},400);
    const controller = new AbortController(), timer = setTimeout(() => controller.abort(),8000);
    try {
      const response = await fetcher(url, {redirect:'manual', signal:controller.signal, headers:{
        'Accept':'application/json', 'User-Agent':'TarClient-Community/1.0 (+https://github.com/TarClient/tar-client)'
      }});
      if (response.status === 204) return new Response(null,{status:204,headers:{'Cache-Control':'no-store'}});
      if (response.status !== 200) {
        await response.body?.cancel();
        // Never forward upstream HTML, redirects, headers or credentials.
        const status = response.status >= 400 && response.status <= 599 ? response.status : 502;
        return reply({error:'Mojang lookup failed',upstreamStatus:response.status},status);
      }
      const profile = JSON.parse(await boundedText(response.body,32768));
      if (!profile || typeof profile.id !== 'string' || !/^[a-f0-9]{32}$/.test(profile.id) || !validName(profile.name) || profile.name.toLowerCase() !== body.name.toLowerCase()) return reply({error:'Invalid Mojang identity'},502);
      return reply({id:profile.id,name:profile.name});
    } catch { return reply({error:'Mojang lookup unavailable'},502); }
    finally { clearTimeout(timer); }
  };
}

export default async request => createVerifier({secret:process.env.TAR_VERIFIER_SECRET})(request);
