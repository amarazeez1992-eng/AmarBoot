package com.personal.gridbot.amaros.design

/**
 * Shared browser-surface performance guard for the animated Amar home.
 * Pauses CSS animation work when the WebView is not visible and honors the
 * platform/browser reduced-motion preference without changing functionality.
 */
object AmarPerformanceController {
    fun script(): String = """
        (function(){
          var id='amarPerformanceStyle';
          var old=document.getElementById(id);
          if(old) old.remove();
          var style=document.createElement('style');
          style.id=id;
          style.textContent=`
            .amar-perf-paused *,
            .amar-perf-paused *::before,
            .amar-perf-paused *::after{
              animation-play-state:paused !important;
              transition:none !important;
            }
            @media (prefers-reduced-motion: reduce){
              *,*::before,*::after{
                animation-duration:0.001ms !important;
                animation-iteration-count:1 !important;
                transition-duration:0.001ms !important;
                scroll-behavior:auto !important;
              }
            }
          `;
          document.head.appendChild(style);

          function sync(){
            document.documentElement.classList.toggle('amar-perf-paused', document.hidden);
          }
          document.addEventListener('visibilitychange', sync, {passive:true});
          sync();
        })();
    """.trimIndent()
}
