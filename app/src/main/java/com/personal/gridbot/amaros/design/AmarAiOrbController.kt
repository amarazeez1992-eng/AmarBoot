package com.personal.gridbot.amaros.design

/** Premium visual controller for the floating AMAR AI orb.
 *  UI-only: does not execute trading commands or change runtime permissions.
 */
object AmarAiOrbController {
    fun script(): String = """
        (function(){
          var a=document.getElementById('amarAiOrb');
          if(!a){
            a=document.createElement('button');
            a.id='amarAiOrb';
            a.type='button';
            a.setAttribute('aria-label','AMAR AI');
            a.onclick=function(){if(window.Android&&Android.openRoom)Android.openRoom('ANALYSIS')};
            var scene=document.querySelector('.scene');
            (scene||document.body).appendChild(a);
          }
          a.innerHTML='<span class="ai-core">AI</span><span class="ai-ring r1"></span><span class="ai-ring r2"></span><span class="ai-ring r3"></span><span class="ai-orbit o1"></span><span class="ai-orbit o2"></span><span class="ai-orbit o3"></span><span class="ai-glass"></span><span class="ai-particle p1"></span><span class="ai-particle p2"></span><span class="ai-particle p3"></span><span class="ai-particle p4"></span><span class="ai-sheen"></span>';
          var s=document.getElementById('amarAiPremiumStyle');
          if(s) s.remove();
          s=document.createElement('style'); s.id='amarAiPremiumStyle';
          s.textContent=`
          #amarAiOrb{--ai-drift:5px;--ai-drift-duration:6.4s;--ai-market-intensity:.0;position:fixed;left:7vw;bottom:7vh;z-index:30;width:86px!important;height:86px!important;padding:0;border:0!important;border-radius:50%!important;background:radial-gradient(circle at 30% 24%,#ffffff 0%,#baf8ff 9%,#39d9ff 25%,#6c4dff 52%,#bd35ff 73%,#16052f 100%)!important;box-shadow:0 0 0 2px rgba(255,255,255,.55),0 0 14px #39d9ff,0 0 38px rgba(108,77,255,.85),0 0 76px rgba(189,53,255,.42),inset 8px 8px 18px rgba(255,255,255,.58),inset -10px -12px 22px rgba(10,0,40,.7)!important;perspective:400px;transform-style:preserve-3d;overflow:visible!important;isolation:isolate;cursor:pointer;transition:filter .25s ease,transform .2s ease;animation:aiOrbDrift var(--ai-drift-duration) ease-in-out infinite}
          #amarAiOrb:before{content:'';position:absolute;inset:-9px;border-radius:50%;background:conic-gradient(from 0deg,transparent 0 8%,#00eaff 13%,transparent 20% 37%,#ff4dff 44%,transparent 51% 70%,#7c5cff 78%,transparent 86%);filter:blur(2px);opacity:.9;z-index:-1;animation:aiHaloSpin 5s linear infinite}
          #amarAiOrb:after{content:'';position:absolute;inset:-17px;border-radius:50%;border:1px solid rgba(80,230,255,.28);box-shadow:0 0 24px rgba(80,230,255,.35);z-index:-2;animation:aiHaloPulse 2.4s ease-in-out infinite}
          #amarAiOrb .ai-core{position:absolute;inset:0;display:grid;place-items:center;color:#fff;font:900 20px/1 Arial,sans-serif;letter-spacing:1px;text-shadow:0 0 7px #fff,0 0 18px #00eaff,0 0 28px #c14cff;z-index:10;animation:aiCorePulse 1.8s ease-in-out infinite}
          #amarAiOrb .ai-glass{position:absolute;inset:5px;border-radius:50%;background:linear-gradient(145deg,rgba(255,255,255,.42),rgba(255,255,255,.04) 38%,rgba(0,0,0,.22) 75%);border:1px solid rgba(255,255,255,.48);z-index:7;pointer-events:none}
          #amarAiOrb .ai-ring{position:absolute;border-radius:50%;border:1px solid;pointer-events:none;z-index:4}
          #amarAiOrb .r1{inset:8px;border-color:rgba(0,239,255,.55);animation:aiRing 4s linear infinite}
          #amarAiOrb .r2{inset:14px;border-color:rgba(255,84,255,.42);animation:aiRingReverse 3.2s linear infinite}
          #amarAiOrb .r3{inset:21px;border-color:rgba(255,255,255,.24);animation:aiRing 2.3s linear infinite}
          #amarAiOrb .ai-orbit{position:absolute;width:7px;height:7px;border-radius:50%;z-index:8;box-shadow:0 0 8px 3px currentColor;pointer-events:none}
          #amarAiOrb .o1{background:#00f6ff;color:#00f6ff;animation:aiOrbit1 2.8s linear infinite}
          #amarAiOrb .o2{background:#ff48ff;color:#ff48ff;animation:aiOrbit2 3.7s linear infinite}
          #amarAiOrb .o3{background:#fff;color:#fff;animation:aiOrbit3 4.6s linear infinite}
          #amarAiOrb .ai-particle{position:absolute;width:3px;height:3px;border-radius:50%;background:#fff;box-shadow:0 0 7px 2px #00eaff;z-index:9;animation:aiParticle 2.6s ease-in-out infinite}
          #amarAiOrb .p1{left:15%;top:30%}.p2{right:16%;top:38%;animation-delay:.5s!important}.p3{left:30%;bottom:17%;animation-delay:1s!important}.p4{right:27%;bottom:23%;animation-delay:1.5s!important}
          #amarAiOrb .ai-sheen{position:absolute;width:38px;height:16px;left:10px;top:10px;border-radius:50%;background:rgba(255,255,255,.75);filter:blur(6px);transform:rotate(-35deg);z-index:11;pointer-events:none;animation:aiSheen 2.8s ease-in-out infinite}
          #amarAiOrb:hover{filter:brightness(1.25) saturate(1.3);transform:scale(1.08)!important}
          #amarAiOrb:active{transform:scale(.94)!important}
          #amarAiOrb.market-buy{box-shadow:0 0 0 2px rgba(255,255,255,.55),0 0 calc(18px + 28px * var(--ai-market-intensity)) #35ffb0,0 0 42px rgba(41,255,176,.55),inset 8px 8px 18px rgba(255,255,255,.58),inset -10px -12px 22px rgba(0,40,28,.68)!important}
          #amarAiOrb.market-sell{box-shadow:0 0 0 2px rgba(255,255,255,.55),0 0 calc(18px + 28px * var(--ai-market-intensity)) #ff4d7d,0 0 42px rgba(255,55,112,.55),inset 8px 8px 18px rgba(255,255,255,.58),inset -10px -12px 22px rgba(55,0,20,.68)!important}
          #amarAiOrb.market-neutral{box-shadow:0 0 0 2px rgba(255,255,255,.55),0 0 18px #39d9ff,0 0 38px rgba(108,77,255,.72),inset 8px 8px 18px rgba(255,255,255,.58),inset -10px -12px 22px rgba(10,0,40,.7)!important}
          @keyframes aiOrbDrift{0%,100%{translate:0 0}50%{translate:0 calc(var(--ai-drift) * -1)}}
          @keyframes aiHaloSpin{to{transform:rotate(360deg)}}
          @keyframes aiHaloPulse{50%{transform:scale(1.14);opacity:.45}}
          @keyframes aiCorePulse{50%{transform:translateZ(14px) scale(1.08);filter:brightness(1.3)}}
          @keyframes aiRing{to{transform:rotate(360deg) scale(1.03)}}
          @keyframes aiRingReverse{to{transform:rotate(-360deg) scale(.96)}}
          @keyframes aiOrbit1{from{transform:rotate(0deg) translateX(35px)}to{transform:rotate(360deg) translateX(35px)}}
          @keyframes aiOrbit2{from{transform:rotate(120deg) translateX(29px)}to{transform:rotate(-240deg) translateX(29px)}}
          @keyframes aiOrbit3{from{transform:rotate(210deg) translateX(39px)}to{transform:rotate(-150deg) translateX(39px)}}
          @keyframes aiParticle{50%{transform:translate(7px,-9px) scale(1.8);opacity:.35}}
          @keyframes aiSheen{0%,100%{transform:translate(-5px,-5px) rotate(-35deg);opacity:.25}50%{transform:translate(40px,35px) rotate(-35deg);opacity:.9}}
          @media (prefers-reduced-motion: reduce){#amarAiOrb{animation-duration:.001ms!important;animation-iteration-count:1!important}#amarAiOrb:before,#amarAiOrb:after,#amarAiOrb .ai-core,#amarAiOrb .ai-ring,#amarAiOrb .ai-orbit,#amarAiOrb .ai-particle,#amarAiOrb .ai-sheen{animation-duration:.001ms!important;animation-iteration-count:1!important}}
          `;
          document.head.appendChild(s);
          window.amarAiOrbSetMarketState=function(direction,intensity,quality,drift,duration){
            if(!a)return;
            var i=Math.max(0,Math.min(1,Number(intensity)||0));
            var d=Math.max(2.5,Math.min(8.5,Number(drift)||5));
            var t=Math.max(5,Math.min(7,Number(duration)||6.4));
            a.style.setProperty('--ai-market-intensity',i.toFixed(3));
            a.style.setProperty('--ai-drift',d.toFixed(2)+'px');
            a.style.setProperty('--ai-drift-duration',t.toFixed(2)+'s');
            a.classList.remove('market-buy','market-sell','market-neutral');
            if(quality==='live' && direction==='buy') a.classList.add('market-buy');
            else if(quality==='live' && direction==='sell') a.classList.add('market-sell');
            else a.classList.add('market-neutral');
            a.dataset.marketDirection=direction||'unknown';
            a.dataset.marketQuality=quality||'unavailable';
          };
          window.amarAiOrbSetMarketState('unknown',0,'unavailable',5,6.4);
        })();
    """.trimIndent()
}
