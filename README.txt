MiniPack - minimal web to desktop packer
Free / no watermark / target PC installs nothing

WHICH OF THE TWO IDEs WORKS
  IntelliJ IDEA 2026.1.3  -> YES. Ships its own JBR with javac + java.
  VS Code                 -> NO. Editor only, no compiler, no runtime.

START.bat does NOT open IDEA and does NOT touch any existing project.
It only borrows javac.exe from the IDEA install folder, then exits.

STEPS (on the Windows 10 packing PC)
  1. Download and unzip Electron 22.3.27 (last version supporting Win7):
     https://github.com/electron/electron/releases/download/v22.3.27/electron-v22.3.27-win32-x64.zip
     If blocked, prefix the whole URL with: https://ghfast.top/
     For 32-bit Win7 use win32-ia32.
     Unzip until you can SEE electron.exe directly in that folder.

  2. Unzip this package to C:\MiniPack\  (do NOT put it inside the kernel folder)

  3. Double click START.bat. Keep the black box open. GUI pops up in 2s.

  4. In the GUI:
     (1) Web folder    = folder containing index.html (no exe / dll / zip inside)
     (2) Icon .ico     = optional, leave empty if none
     (3) Kernel folder = the folder where electron.exe is visible directly
     (4) Output folder = a NEW EMPTY folder, e.g. C:\out\
     Then fill: window title / product name / company / copyright / version / W x H
     Click the big green button.

  5. Black box prints 1/6 ... 6/6 OK. Done.

  6. Send the WHOLE C:\out\ folder to the Win7 PC via QQ,
     save + unzip there, double click the exe inside. Never send exe alone.

NOTES
  - This is an assembler, not a compiler: no download, no Python, no Node.
    Every step writes a log line, so it cannot fail silently.
  - Kernel is Chromium 108 (same family as NW.js). Zoom, x/y are pinned and
    GPU disabled to fight window drift. If a stripped Win7 still exits silently,
    that is the OS graphics stack, not this tool.
  - Window + taskbar icon always works. To also set the exe file icon and
    right-click Properties, drop rcedit.exe next to START.bat:
    https://github.com/electron/rcedit/releases

COPYRIGHT / NOTICE
  Copyright (c) 2026 <YOUR NAME OR GITHUB ID>. MIT License (LICENSE).
  A floating copyright window pops up on every GUI start. Do NOT pass this
  tool off as your own: keep LICENSE, NOTICE.txt, the source header and the
  in-app notice in every copy. See NOTICE.txt for the full statement.
  Replace the <YOUR NAME OR GITHUB ID> placeholder before publishing.

IF START.bat SAYS "javac.exe not found"
  IDEA: File -> New -> Project -> Java -> (no Maven/Gradle) -> name MiniPackTool
        -> Location: a BRAND NEW folder, not your mom's project -> Finish
  Drag MiniPack.java into src, right click it -> Run 'MiniPack.main()'
  The same GUI appears, continue from step 4.
