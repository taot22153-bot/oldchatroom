const BASE_URL = window.CHAT_URL || '/chat';

function loadMessages() {
    fetch(BASE_URL)
        .then(async r => {
            if (!r.ok) throw new Error('HTTP ' + r.status);
            const t = await r.text();
            try { return JSON.parse(t); }
            catch { throw new Error('JSON解析失败：' + t.slice(0,200)); }
        })
        .then(data => {
            const container = document.querySelector('#messages');
            const meId = data.me?.id || '';
            if (!data.messages || data.messages.length === 0) {
                container.innerHTML = `<div class="system-msg">暂无消息，试着发送一条吧～</div>`;
                return;
            }
            container.innerHTML = data.messages.map(msg => {
                if (msg.type === 'system') return `<div class="system-msg">📢 ${msg.text}</div>`;
                const side = (msg.senderId === meId) ? 'msg-right' : 'msg-left';
                return `<div class="${side}"><div class="msg-bubble">${msg.senderName}: ${msg.text}</div></div>`;
            }).join('');
            container.scrollTop = container.scrollHeight;
        })
        .catch(err => {
            console.error('加载失败:', err);
            document.querySelector('#messages').innerHTML =
                `<div class="system-msg">⚠️ 加载失败：${String(err.message).replaceAll('<','&lt;')}</div>`;
        });
}

document.addEventListener('DOMContentLoaded', () => {
    document.querySelector('#msgForm').addEventListener('submit', e => {
        e.preventDefault();
        const msg = document.querySelector('#msgInput').value.trim();
        if (!msg) return;
        fetch(BASE_URL, {
            method: 'POST',
            headers: {'Content-Type':'application/x-www-form-urlencoded'},
            body: 'action=send&msg=' + encodeURIComponent(msg)
        }).then(() => {
            document.querySelector('#msgInput').value = '';
            loadMessages();
        });
    });

    document.querySelector('#nameForm').addEventListener('submit', e => {
        e.preventDefault();
        const name = document.querySelector('#nameInput').value.trim();
        if (!name) return;
        fetch(BASE_URL, {
            method: 'POST',
            headers: {'Content-Type':'application/x-www-form-urlencoded'},
            body: 'action=setName&name=' + encodeURIComponent(name)
        }).then(() => {
            document.querySelector('#nameInput').value = '';
            loadMessages();
        });
    });

    loadMessages();
    setInterval(loadMessages, 2000);
});