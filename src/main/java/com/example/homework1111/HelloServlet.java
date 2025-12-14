// File: HelloServlet.java
package com.example.homework1111;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.ServletContext;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

@WebServlet(name = "helloServlet", value = "/chat")
public class HelloServlet extends HttpServlet {

    // ======== 数据模型 ========
    private static class Message implements java.io.Serializable {
        public String type;       // "user" | "system"
        public String senderId;
        public String senderName; // 未设置昵称时用空字符串
        public String text;
        public long ts;
        Message(String type, String id, String name, String text, long ts) {
            this.type = type; this.senderId = id; this.senderName = name; this.text = text; this.ts = ts;
        }
    }

    // ======== 初始化内存存储（无磁盘 I/O） ========
    @Override public void init() {
        ServletContext ctx = getServletContext();
        if (ctx.getAttribute("messages") == null)
            ctx.setAttribute("messages", new CopyOnWriteArrayList<Message>());
        if (ctx.getAttribute("nameBook") == null)
            ctx.setAttribute("nameBook", new ConcurrentHashMap<String, String>()); // sessionId -> 昵称
    }

    @SuppressWarnings("unchecked")
    private List<Message> messages() {
        return (List<Message>) getServletContext().getAttribute("messages");
    }
    @SuppressWarnings("unchecked")
    private Map<String, String> nameBook() {
        return (Map<String, String>) getServletContext().getAttribute("nameBook");
    }

    // ======== 拉取数据 ========
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession();
        String sid = session.getId();

        String meName = (String) session.getAttribute("name");
        if (meName == null) meName = nameBook().get(sid);
        if (meName == null) meName = ""; // 去掉“匿名”，未设置昵称时返回空字符串

        Map<String, Object> result = new HashMap<>();
        result.put("messages", messages());
        result.put("me", Map.of("id", sid, "name", meName));

        resp.setContentType("application/json;charset=UTF-8");
        com.google.gson.Gson gson = new com.google.gson.Gson();
        resp.getWriter().write(gson.toJson(result));
    }

    // ======== 发送消息 / 设置昵称 ========
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding(StandardCharsets.UTF_8.name());
        HttpSession session = req.getSession();
        String sid = session.getId();

        String action = Optional.ofNullable(req.getParameter("action")).orElse("send");
        long now = System.currentTimeMillis();

        if ("setName".equals(action)) {
            String newName = trimOrNull(req.getParameter("name"));
            if (newName == null) {
                writeJson(resp, Map.of("status","error","msg","昵称不能为空"));
                return;
            }
            String old = nameBook().put(sid, newName);
            session.setAttribute("name", newName);
            if (old == null) {
                messages().add(new Message("system", sid, "系统", newName + " 进入聊天室", now));
            } else if (!old.equals(newName)) {
                messages().add(new Message("system", sid, "系统", old + " 改名为 " + newName, now));
            }
            writeJson(resp, Map.of("status","ok"));
            return;
        }

        // action = send
        String text = trimOrNull(req.getParameter("msg"));
        if (text != null) {
            String showName = (String) session.getAttribute("name");
            if (showName == null) showName = nameBook().get(sid);
            if (showName == null) showName = ""; // 不再使用“匿名xxxx”
            nameBook().putIfAbsent(sid, showName);
            messages().add(new Message("user", sid, showName, text, now));
        }
        writeJson(resp, Map.of("status","ok"));
    }

    // ======== 工具方法 ========
    private static String trimOrNull(String s) {
        if (s == null) return null;
        s = s.trim();
        return s.isEmpty() ? null : s;
    }
    private static void writeJson(HttpServletResponse resp, Object obj) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        com.google.gson.Gson gson = new com.google.gson.Gson();
        resp.getWriter().write(gson.toJson(obj));
    }
}