<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>在线聊天室</title>
    <link rel="stylesheet" href="style.css">
</head>
<body>
<h1>在线聊天室</h1>

<div id="messages">加载中...</div>

<form id="nameForm">
    <input type="text" id="nameInput" placeholder="设置昵称" required style="width:200px;">
    <button type="submit">修改昵称</button>
</form>

<form id="msgForm">
    <input type="text" id="msgInput" placeholder="说点什么..." required style="width:300px;">
    <button type="submit">发送</button>
</form>

<!-- 把真实上下文路径交给 JS -->
<script>
    window.CHAT_URL = '<%= request.getContextPath() %>/chat';
    window.MY_SESSION_ID = '<%= session.getId() %>';
    console.log('CTX =', window.CHAT_URL); // 看到这行说明 JS 已成功加载
</script>
<script src="javascript.js"></script>
</body>
</html>