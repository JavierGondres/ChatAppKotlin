const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");
const { getMessaging } = require("firebase-admin/messaging");

initializeApp();

exports.notifyNewMessage = onDocumentCreated(
  "conversations/{conversationId}/messages/{messageId}",
  async (event) => {
    const snapshot = event.data;
    if (!snapshot) return;

    const message = snapshot.data();
    const receiverId = message.receiverId;
    const senderId = message.senderId;
    if (!receiverId || receiverId === senderId) return;

    const userSnapshot = await getFirestore().collection("users").doc(receiverId).get();
    const token = userSnapshot.get("fcmToken");
    if (!token) return;

    const text = typeof message.text === "string" ? message.text.trim() : "";
    const body = text || "Imagen";
    const senderName = message.senderName || "Nuevo mensaje";

    await getMessaging().send({
      token,
      data: {
        conversationId: event.params.conversationId,
        otherUserId: String(senderId),
        senderName: String(senderName),
        body,
      },
      android: {
        priority: "high",
      },
    });
  }
);
