package com.personal.gridbot.amaros.design

/** Independent premium digital clock controller for the home WebView. */
object AmarDigitalClockController {
    fun script(): String = """
        (function(){
          var c=document.getElementById('clock');
          if(!c)return;
          if(window.__amarDigitalClockTimer)clearInterval(window.__amarDigitalClockTimer);
          c.className='amar-digital-clock';
          c.innerHTML='<span class="adc-time">00:00:00</span>';
          c.style.cssText='position:relative;width:128px;height:58px;border-radius:18px;display:flex;align-items:center;justify-content:center;box-sizing:border-box;overflow:hidden;direction:ltr;background:linear-gradient(145deg,rgba(7,17,31,.96),rgba(25,19,58,.94));border:1px solid rgba(76,229,255,.82);box-shadow:0 0 0 1px rgba(176,92,255,.25),0 0 26px rgba(0,217,255,.34),0 0 48px rgba(125,75,255,.16),inset 0 0 22px rgba(76,224,255,.13);backdrop-filter:blur(14px);font-family:monospace;z-index:20;';
          var style=document.getElementById('amarDigitalClockStyle');
          if(!style){style=document.createElement('style');style.id='amarDigitalClockStyle';document.head.appendChild(style)}
          style.textContent=`
            #clock.amar-digital-clock:before{content:"";position:absolute;inset:-55%;background:conic-gradient(from 0deg,transparent,#00eaff30,transparent,#9b5cff38,transparent);animation:adcSpin 9s linear infinite;z-index:0}
            #clock.amar-digital-clock:after{content:"";position:absolute;left:-45%;top:-10%;width:32%;height:120%;background:linear-gradient(90deg,transparent,#ffffffaa,transparent);transform:skewX(-18deg);animation:adcShine 3.6s ease-in-out infinite;z-index:2}
            #clock.amar-digital-clock .adc-time{position:relative;z-index:3;font-size:20px;font-weight:1000;letter-spacing:2px;color:#b9fbff;text-shadow:0 0 6px #00eaff,0 0 14px #4dcfff,0 0 24px #8d63ff;line-height:1;white-space:nowrap;font-variant-numeric:tabular-nums}
            @keyframes adcSpin{to{transform:rotate(360deg)}}
            @keyframes adcShine{0%,35%{left:-45%;opacity:0}50%{opacity:1}70%,100%{left:125%;opacity:0}}
            @keyframes adcPulse{50%{filter:brightness(1.15)}}
          `;
          function tick(){
            var d=new Date();
            var p=function(n){return String(n).padStart(2,'0')};
            var time=p(d.getHours())+':'+p(d.getMinutes())+':'+p(d.getSeconds());
            var node=c.querySelector('.adc-time');
            if(node)node.textContent=time;
            c.style.animation='adcPulse 3s ease-in-out infinite';
          }
          tick();
          window.__amarDigitalClockTimer=setInterval(tick,1000);
        })();
    """.trimIndent()
}
