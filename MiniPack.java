import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/*
 * MiniPack —— 极简网页打包器（免费 / 无水印 / 目标机不装任何东西）
 * 只做一件事：把你选好的网页 + Electron 绿色内核组装成一个目录，
 * 顺手把图标、窗口标题、产品名、公司、版权、版本、窗口尺寸写进去。
 *
 * Copyright (c) 2026 <YOUR NAME OR GITHUB ID>
 * 本软件著作权归作者所有，基于 MIT License 分发（见 LICENSE / NOTICE.txt）。
 * 禁止把本软件当成自己的软件发给别人：再分发时必须保留本声明、
 * LICENSE 与 NOTICE.txt，不得删除署名或宣称自己是原作者。
 */
public class MiniPack {

    static JTextField fWeb, fIcon, fKernel, fOut;
    static JTextField fTitle, fProduct, fCompany, fCopyright, fVersion, fW, fH;
    static JTextArea log;

    static String v(JTextField t) { return t.getText().trim(); }

    static String js(String s) {
        return s.replace("\\", "\\\\").replace("'", "\\'")
                .replace("\r", " ").replace("\n", " ");
    }
    static String json(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\r", " ").replace("\n", " ");
    }

    static void say(String s) { log.append(s + "\n"); log.setCaretPosition(log.getDocument().getLength()); }

    static String pickDir(JFrame p, String cur) {
        JFileChooser c = new JFileChooser(cur.isEmpty() ? "C:\\" : cur);
        c.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        c.setDialogTitle("选择文件夹");
        return c.showOpenDialog(p) == JFileChooser.APPROVE_OPTION ? c.getSelectedFile().getAbsolutePath() : cur;
    }
    static String pickFile(JFrame p, String cur, String ext) {
        JFileChooser c = new JFileChooser(cur.isEmpty() ? "C:\\" : cur);
        c.setDialogTitle("选择 " + ext + " 文件");
        return c.showOpenDialog(p) == JFileChooser.APPROVE_OPTION ? c.getSelectedFile().getAbsolutePath() : cur;
    }

    static void row(JPanel p, String label, JTextField field, String btn, Runnable act) {
        JPanel r = new JPanel(new BorderLayout(6, 0));
        r.setMaximumSize(new Dimension(900, 30));
        r.add(new JLabel(label), BorderLayout.WEST);
        r.add(field, BorderLayout.CENTER);
        if (btn != null) {
            JButton b = new JButton(btn);
            b.setPreferredSize(new Dimension(72, 24));
            b.addActionListener(e -> act.run());
            r.add(b, BorderLayout.EAST);
        }
        p.add(r);
        p.add(Box.createVerticalStrut(5));
    }

    static JTextField tf(String def, int w) {
        JTextField t = new JTextField(def, w);
        t.setMaximumSize(new Dimension(900, 26));
        return t;
    }

    /** 置顶悬浮版权窗：启动即弹，点按钮或点窗内任意处关闭 */
    static void showNotice(JFrame owner) {
        final JWindow w = new JWindow(owner);
        w.setAlwaysOnTop(true);

        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBackground(new Color(0xFF, 0xFB, 0xE6));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x00, 0x78, 0xD7), 2),
                new EmptyBorder(12, 16, 10, 16)));

        JLabel t = new JLabel(
            "<html><div style='width:320px; line-height:1.5;'>"
            + "<b style='color:#c00000; font-size:13px;'>版权声明 · MiniPack 极简打包器</b><br><br>"
            + "Copyright (c) 2026 &lt;YOUR NAME OR GITHUB ID&gt;<br>"
            + "本软件著作权归作者所有，基于 MIT License 分发。<br><br>"
            + "<b style='color:#c00000;'>禁止把本软件当成自己的软件发给别人。</b><br>"
            + "再分发必须保留本声明、LICENSE 与 NOTICE.txt；<br>"
            + "不得删除署名，不得宣称自己是原作者。"
            + "</div></html>");
        t.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

        JButton close = new JButton("知道了  ×");
        close.setFocusPainted(false);
        close.addActionListener(e -> w.dispose());

        JPanel bot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bot.setOpaque(false);
        bot.add(close);

        p.add(t, BorderLayout.CENTER);
        p.add(bot, BorderLayout.SOUTH);
        p.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) { w.dispose(); }
        });

        w.setContentPane(p);
        w.pack();
        Point loc = owner.getLocationOnScreen();
        w.setLocation(loc.x + owner.getWidth() - w.getWidth() - 16, loc.y + 34);
        w.setVisible(true);
    }

    static void copyTree(Path src, Path dst) throws IOException {
        Files.walk(src).forEach(p -> {
            try {
                Path rel = src.relativize(p);
                Path to = dst.resolve(rel.toString());
                if (Files.isDirectory(p)) Files.createDirectories(to);
                else { Files.createDirectories(to.getParent()); Files.copy(p, to, StandardCopyOption.REPLACE_EXISTING); }
            } catch (IOException e) { throw new RuntimeException(e); }
        });
    }

    static void write(Path p, String s) throws IOException {
        Files.createDirectories(p.getParent());
        Files.write(p, s.getBytes("UTF-8"));
    }

    static void build() {
        try {
            Path web = Paths.get(v(fWeb)), kernel = Paths.get(v(fKernel)), out = Paths.get(v(fOut));
            if (!Files.isDirectory(web) || !Files.exists(web.resolve("index.html"))) { say("✗ 网页文件夹里没有 index.html"); return; }
            if (!Files.isDirectory(kernel) || !Files.exists(kernel.resolve("electron.exe"))) { say("✗ 内核目录里没有 electron.exe（要选解压后的那一层）"); return; }
            if (v(fOut).isEmpty()) { say("✗ 先选输出目录"); return; }
            if (out.startsWith(kernel) || out.startsWith(web)) { say("✗ 输出目录不能放在内核或网页文件夹里面"); return; }
            Files.createDirectories(out);

            say("1/6 复制内核…");
            copyTree(kernel, out);

            Path app = out.resolve("resources").resolve("app");
            Files.createDirectories(app.resolve("www"));
            say("2/6 复制网页…");
            copyTree(web, app.resolve("www"));

            String iconRel = "icon.ico";
            if (!v(fIcon).isEmpty() && Files.exists(Paths.get(v(fIcon)))) {
                Files.copy(Paths.get(v(fIcon)), app.resolve("icon.ico"), StandardCopyOption.REPLACE_EXISTING);
                say("3/6 图标已放入");
            } else { say("3/6 没选图标，跳过"); iconRel = ""; }

            String W = v(fW).isEmpty() ? "1024" : v(fW), H = v(fH).isEmpty() ? "768" : v(fH);
            String ver = v(fVersion).isEmpty() ? "1.0.0" : v(fVersion);
            String title = v(fTitle).isEmpty() ? "我的应用" : v(fTitle);
            String product = v(fProduct).isEmpty() ? title : v(fProduct);
            String company = v(fCompany), copyright = v(fCopyright);

            String iconLine = iconRel.isEmpty() ? "" :
                    ",\n    icon: __dirname + '/icon.ico'";

            String mainJs =
                "const { app, BrowserWindow } = require('electron');\n" +
                "app.disableHardwareAcceleration();\n" +
                "app.commandLine.appendSwitch('disable-gpu');\n" +
                "app.commandLine.appendSwitch('force-device-scale-factor', '1');\n" +
                "app.commandLine.appendSwitch('high-dpi-support', '1');\n" +
                "app.commandLine.appendSwitch('autoplay-policy', 'no-user-gesture-required');\n" +
                "let win;\n" +
                "app.whenReady().then(function () {\n" +
                "  win = new BrowserWindow({\n" +
                "    width: " + W + ", height: " + H + ",\n" +
                "    x: 80, y: 40, center: false,\n" +
                "    resizable: true, movable: true, fullscreenable: false,\n" +
                "    autoHideMenuBar: true,\n" +
                "    title: '" + js(title) + "'" + iconLine + ",\n" +
                "    webPreferences: { zoomFactor: 1.0, nodeIntegration: false, contextIsolation: true }\n" +
                "  });\n" +
                "  win.setMenu(null);\n" +
                "  win.loadFile(__dirname + '/www/index.html');\n" +
                "});\n" +
                "app.on('window-all-closed', function () { app.quit(); });\n";

            String pkg = "{\n" +
                "  \"name\": \"minipack-app\",\n" +
                "  \"version\": \"" + json(ver) + "\",\n" +
                "  \"description\": \"" + json(product) + "\",\n" +
                "  \"author\": \"" + json(company) + "\",\n" +
                "  \"main\": \"main.js\"\n" +
                "}\n";

            say("4/6 写配置…");
            write(app.resolve("main.js"), mainJs);
            write(app.resolve("package.json"), pkg);
            // 去掉默认应用，避免某些版本优先加载它
            Files.deleteIfExists(out.resolve("resources").resolve("default_app.asar"));

            String meta = "产品名：" + product + "\r\n窗口标题：" + title +
                    "\r\n公司/作者：" + company + "\r\n版权：" + copyright +
                    "\r\n版本：" + ver + "\r\n窗口：" + W + " x " + H +
                    "\r\n生成时间：" + new Date() + "\r\n";
            write(out.resolve("版本信息.txt"), meta);

            say("5/6 改 exe 名字…");
            String safe = product.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
            if (safe.isEmpty()) safe = "myapp";
            Path exe = out.resolve("electron.exe");
            Path named = out.resolve(safe + ".exe");
            if (Files.exists(exe)) {
                Files.copy(exe, named, StandardCopyOption.REPLACE_EXISTING);
                Files.delete(exe);
                say("    已生成 " + named.getFileName() + "（双击没反应就把名字改回 electron.exe）");
            }

            // 可选：工具目录里放了 rcedit.exe 就顺便把图标和版权写进 exe 属性
            Path rcedit = Paths.get("rcedit.exe");
            if (Files.exists(rcedit) && Files.exists(named) && !iconRel.isEmpty()) {
                say("6/6 写入 exe 图标与版本信息…");
                java.util.List<String> cmd = new ArrayList<>(Arrays.asList(
                    rcedit.toAbsolutePath().toString(), named.toAbsolutePath().toString(),
                    "--set-icon", app.resolve("icon.ico").toAbsolutePath().toString(),
                    "--set-version-string", "ProductName", product,
                    "--set-version-string", "FileDescription", title,
                    "--set-version-string", "CompanyName", company,
                    "--set-version-string", "LegalCopyright", copyright,
                    "--set-version-string", "FileVersion", ver,
                    "--set-version-string", "ProductVersion", ver));
                try { new ProcessBuilder(cmd).inheritIO().start().waitFor(); }
                catch (Exception ex) { say("    rcedit 没跑成，不影响启动"); }
            } else {
                say("6/6 完成（没放 rcedit.exe，exe 文件图标用默认；窗口/任务栏图标已生效）");
            }

            say("✓ 好了：把【整个输出文件夹】拷到 Win7，双击 " + safe + ".exe");
            say("  输出位置：" + out.toAbsolutePath());
        } catch (Exception e) {
            say("✗ 出错：" + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        JFrame f = new JFrame("MiniPack · 极简打包器");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(760, 640);
        f.setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(new EmptyBorder(10, 12, 10, 12));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(4, 4, 4, 4));

        fWeb = tf("", 30); fIcon = tf("", 30); fKernel = tf("", 30); fOut = tf("", 30);
        fTitle = tf("My Web App", 30);
        fProduct = tf("MiniPack App", 30);
        fCompany = tf("Your Name", 30);
        fCopyright = tf("Copyright (c) 2026 Your Name", 30);
        fVersion = tf("1.0.0", 30);
        fW = tf("1024", 8); fH = tf("768", 8);

        JPanel sizeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        sizeRow.setMaximumSize(new Dimension(900, 28));
        sizeRow.add(new JLabel("窗口宽 × 高")); sizeRow.add(fW); sizeRow.add(new JLabel("×")); sizeRow.add(fH);
        fW.setPreferredSize(new Dimension(90, 24)); fH.setPreferredSize(new Dimension(90, 24));
        fW.setMaximumSize(new Dimension(90, 24)); fH.setMaximumSize(new Dimension(90, 24));

        row(form, "① 网页文件夹  ", fWeb, "选择…", () -> fWeb.setText(pickDir(f, v(fWeb))));
        row(form, "② 图标 .ico     ", fIcon, "选择…", () -> fIcon.setText(pickFile(f, v(fIcon), "ico")));
        row(form, "③ 内核目录      ", fKernel, "选择…", () -> fKernel.setText(pickDir(f, v(fKernel))));
        row(form, "④ 输出目录      ", fOut, "选择…", () -> fOut.setText(pickDir(f, v(fOut))));
        form.add(new JSeparator()); form.add(Box.createVerticalStrut(6));
        row(form, "窗口标题        ", fTitle, null, null);
        row(form, "产品名          ", fProduct, null, null);
        row(form, "公司 / 作者     ", fCompany, null, null);
        row(form, "版权            ", fCopyright, null, null);
        row(form, "版本            ", fVersion, null, null);
        form.add(sizeRow); form.add(Box.createVerticalStrut(8));

        JButton go = new JButton("一键生成");
        go.setFont(go.getFont().deriveFont(Font.BOLD, 15f));
        go.setBackground(new Color(0x00, 0x78, 0xD7));
        go.setForeground(Color.WHITE);
        go.setFocusPainted(false);
        go.setAlignmentX(Component.CENTER_ALIGNMENT);
        go.setMaximumSize(new Dimension(900, 38));
        go.addActionListener(e -> { log.setText(""); build(); });
        form.add(go);

        log = new JTextArea(9, 60);
        log.setEditable(false);
        log.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        log.setBackground(new Color(0x1e, 0x1e, 0x1e));
        log.setForeground(new Color(0xd0, 0xd0, 0xd0));
        log.setBorder(new EmptyBorder(8, 8, 8, 8));
        log.setText("填好 ①②③④ → 点「一键生成」。\n");

        root.add(new JScrollPane(form), BorderLayout.CENTER);
        root.add(new JScrollPane(log), BorderLayout.SOUTH);
        f.setContentPane(root);
        f.setVisible(true);

        log.append("Copyright (c) 2026 <YOUR NAME OR GITHUB ID> · MIT License · 见 NOTICE.txt\n");
        showNotice(f);
    }
}
