package com.personal.gridbot.amaros.design

/** Independent visual controller for the home News/Market/Sessions gate. */
object AmarNewsGateController {
    fun script(): String = """
        (function(){
          var g=document.getElementById('amarNewsGate');
          if(!g)return;
          g.style.transformOrigin='50% 50%';
          g.style.willChange='transform,filter,background-position';
          g.style.background='linear-gradient(120deg,rgba(17,43,85,.92),rgba(91,27,111,.92),rgba(12,111,126,.92),rgba(17,43,85,.92))';
          g.style.backgroundSize='260% 260%';
          var s=document.getElementById('amarNewsGateStyle');
          if(!s){s=document.createElement('style');s.id='amarNewsGateStyle';document.head.appendChild(s)}
          s.textContent=`
            #amarNewsGate{animation:amarNewsFloat 3.6s ease-in-out infinite alternate,amarNewsGradient 7s ease-in-out infinite;box-shadow:0 0 18px rgba(49,231,255,.20),0 0 30px rgba(236,72,153,.14),inset 0 0 18px rgba(255,200,87,.10);}
            #amarNewsGate:before{content:"";position:absolute;inset:-35%;border-radius:inherit;background:conic-gradient(from 0deg,transparent,#31e7ff55,transparent,#ec489955,transparent,#ffc85755,transparent);animation:amarNewsRing 8s linear infinite;pointer-events:none;}
            #amarNewsGate:after{content:"";position:absolute;inset:1px;border-radius:inherit;background:linear-gradient(110deg,transparent,rgba(255,255,255,.16),transparent);background-size:220% 100%;animation:amarNewsSheen 3.2s ease-in-out infinite alternate;pointer-events:none;}
            @keyframes amarNewsFloat{from{translate:0 2px;scale:.99;filter:brightness(.96)}to{translate:0 -5px;scale:1.015;filter:brightness(1.12)}}
            @keyframes amarNewsGradient{0%,100%{background-position:0% 50%}50%{background-position:100% 50%}}
            @keyframes amarNewsRing{to{transform:rotate(360deg)}}
            @keyframes amarNewsSheen{from{background-position:-120% 0}to{background-position:120% 0}}
            @media (prefers-reduced-motion:reduce){#amarNewsGate,#amarNewsGate:before,#amarNewsGate:after{animation:none}}
          `;
        })();
    """.trimIndent()
}
