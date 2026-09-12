package com.personal.gridbot.amaros.design

/** Independent compact premium live clock controller for the home WebView. */
object AmarDigitalClockController {
    fun script(): String = """
        (function(){
          var c=document.getElementById('clock');
          if(!c)return;
          if(window.__amarDigitalClockTimer)clearTimeout(window.__amarDigitalClockTimer);
          c.className='amar-digital-clock';
          c.innerHTML='<span class="adc-label">AMAR TIME</span><span class="adc-time">00:00:00</span><span class="adc-date">--/--/----</span>';
          c.style.cssText='position:relative;width:112px;height:62px;border-radius:18px;display:flex;flex-direction:column;align-items:center;justify-content:center;gap:1px;box-sizing:border-box;overflow:hidden;direction:ltr;background:linear-gradient(145deg,rgba(21,9,55,.96),rgba(8,49,72,.96) 38%,rgba(76,12,68,.94) 72%,rgba(10,31,52,.98));border:1px solid rgba(255,200,87,.92);box-shadow:0 0 0 1px rgba(49,231,255,.30),0 0 22px rgba(49,231,255,.22),0 0 30px rgba(168,85,247,.20),inset 0 0 20px rgba(236,72,153,.18);backdrop-filter:blur(14px);font-family:monospace;z-index:20;';
          var style=document.getElementById('amarDigitalClockStyle');
          if(!style){style=document.createElement('style');style.id='amarDigitalClockStyle';document.head.appendChild(style)}
          style.textContent=`
            #clock.amar-digital-clock:before{content:"";position:absolute;inset:-70%;background:conic-gradient(from 0deg,transparent 0deg,#31e7ff88 42deg,transparent 88deg,#a855f788 142deg,transparent 194deg,#ec489988 248deg,transparent 300deg,#ffc85788 344deg,transparent 360deg);animation:adcSpin 9s linear infinite;z-index:0}
            #clock.amar-digital-clock:after{content:"";position:absolute;inset:1px;border-radius:17px;background:linear-gradient(115deg,transparent 0%,rgba(49,231,255,.16) 23%,transparent 44%,rgba(255,200,87,.13) 65%,rgba(236,72,153,.12) 82%,transparent 100%);background-size:220% 220%;animation:adcSheen 3.6s ease-in-out infinite alternate;z-index:1;pointer-events:none}
            #clock.amar-digital-clock{animation:adcFloat 3.2s ease-in-out infinite alternate}
            #clock.amar-digital-clock .adc-label,#clock.amar-digital-clock .adc-time,#clock.amar-digital-clock .adc-date{position:relative;z-index:2;text-align:center;font-variant-numeric:tabular-nums;white-space:nowrap}
            #clock.amar-digital-clock .adc-label{font-size:5px;font-weight:1000;letter-spacing:2px;color:#7eeeff;text-shadow:0 0 7px #31e7ff;line-height:7px}
            #clock.amar-digital-clock .adc-time{font-size:17px;font-weight:1000;letter-spacing:1.5px;color:#fff0bb;text-shadow:0 0 5px #ffc857,0 0 10px #31e7ff,0 0 17px #a855f7;line-height:20px}
            #clock.amar-digital-clock .adc-date{font-size:7px;font-weight:900;letter-spacing:1px;color:#e8ddff;text-shadow:0 0 7px #ec4899;line-height:9px}
            @keyframes adcSpin{to{transform:rotate(360deg)}}
            @keyframes adcSheen{from{background-position:0% 50%;filter:brightness(.95)}to{background-position:100% 50%;filter:brightness(1.14)}}
            @keyframes adcFloat{from{transform:translateY(2px) scale(.985)}to{transform:translateY(-2px) scale(1.015)}}
            @media (prefers-reduced-motion:reduce){#clock.amar-digital-clock,#clock.amar-digital-clock:before,#clock.amar-digital-clock:after{animation:none}}
          `;
          function tick(){
            var d=new Date();
            var p=function(n){return String(n).padStart(2,'0')};
            var time=p(d.getHours())+':'+p(d.getMinutes())+':'+p(d.getSeconds());
            var date=p(d.getDate())+'/'+p(d.getMonth()+1)+'/'+d.getFullYear();
            var timeNode=c.querySelector('.adc-time');
            var dateNode=c.querySelector('.adc-date');
            if(timeNode)timeNode.textContent=time;
            if(dateNode)dateNode.textContent=date;
            var delay=1000-(Date.now()%1000)+8;
            window.__amarDigitalClockTimer=setTimeout(tick,delay);
          }
          tick();
        })();
    """.trimIndent()
}
