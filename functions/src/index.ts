// ------------------------
// 📌 필요한 import
// ------------------------
import { onDocumentCreated } from "firebase-functions/v2/firestore";
import axios from "axios";
import * as logger from "firebase-functions/logger";
import { defineSecret } from "firebase-functions/params";

// ------------------------
// 📌 SECRET 정의 (🔥 함수 밖에서 선언해야 함 ❗)
// ------------------------
const HF_API_KEY = defineSecret("huggingface_key");

// ------------------------
// 📌 감정 축소 함수 (5 → 3)
// ------------------------
const simplifySentiment = (
  label: string
): "Positive" | "Neutral" | "Negative" => {
  if (label === "Very Positive" || label === "Positive") return "Positive";
  if (label === "Neutral") return "Neutral";
  return "Negative"; // Very Negative / Negative → Negative 처리
};
// 감정 한글 변환 함수
const toKorean = (label: string): string => {
  if (label === "Positive") return "긍정";
  if (label === "Neutral")  return "중립";
  return "부정"; // Negative / Very Negative
};
// ------------------------
// 📌 Cloud Function
// ------------------------
export const analyzeSentimentV2 = onDocumentCreated(
  {
    document: "users/{userId}/diaries/{docId}",
    region: "asia-northeast3",
    secrets: [HF_API_KEY],
  },
  async (event) => {
    // 🔑 context 필드 읽기!!!!
    const text = event.data?.data()?.content; // 🔥 중요!!!
    const apiKey = HF_API_KEY.value();

    if (!text) {
      logger.error("🔥 No 'content' field found in document.");
      return;
    }

    if (!apiKey) {
      logger.error("🚨 Missing API Key!");
      return;
    }

    try {
      // 🚀 API 호출
      const response = await axios.post(
        "https://router.huggingface.co/hf-inference/models/tabularisai/multilingual-sentiment-analysis",
        { inputs: text },
        {
          headers: { Authorization: `Bearer ${apiKey}` },
        }
      );

      const result = response.data;
      logger.info("Raw sentiment API response:", result);

      const now = Date.now();

      if (!Array.isArray(result)) {
        const estimatedTime =
          typeof result?.estimated_time === "number"
            ? result.estimated_time
            : null;
        if (estimatedTime) {
          logger.info(
            `Model is still loading. Estimated time: ${estimatedTime}s`
          );
        }

        await event.data?.ref.set(
          {
            sentiment: estimatedTime ? "Model Loading" : "Unknown",
            score: null,
            analyzedAt: now,
            rawResponse: result,
            modelLoading: Boolean(estimatedTime),
            estimatedTime,
          },
          { merge: true }
        );
        return;
      }

      const candidates: any[] = Array.isArray(result[0]) ? result[0] : result;
      const best =
        candidates.length > 0
          ? [...candidates].sort(
              (a: any, b: any) => (b?.score ?? 0) - (a?.score ?? 0)
            )[0]
          : null;

      if (!best || typeof best.label !== "string") {
        await event.data?.ref.set(
          {
            sentiment: "Unknown",
            score: null,
            analyzedAt: now,
            rawResponse: null,
            modelLoading: false,
            estimatedTime: null,
          },
          { merge: true }
        );
        return;
      }

      const simplified = simplifySentiment(best.label);
      const koreanLabel = toKorean(simplified);
      const score = typeof best.score === "number" ? best.score : null;

      // 🔥 Firestore 업데이트 (`content` 분석 결과 저장)
      await event.data?.ref.set(
        {
          sentiment: koreanLabel,
          score,
          analyzedAt: now,
          rawResponse: null,
          modelLoading: false,
          estimatedTime: null,
        },
        { merge: true }
      );

      logger.info("🎉 Sentiment updated:", simplified, score);
    } catch (err) {
      logger.error("❌ API Error:", err);
      await event.data?.ref.set(
        {
          sentiment: "Unknown",
          score: null,
          analyzedAt: Date.now(),
          error: String(err),
          modelLoading: false,
          estimatedTime: null,
        },
        { merge: true }
      );
    }
  }
);
import { onCall, HttpsError } from "firebase-functions/v2/https";
import * as admin from "firebase-admin";

admin.initializeApp(); // 최상단에 최소 1번만 있어야 함

export const pickRandomUser = onCall(
{
region: "asia-northeast3",
},
async (request) => {
  const senderId = request.auth?.uid;

  if (!senderId) {
    throw new HttpsError("unauthenticated", "로그인이 필요합니다.");
  }

  // Firestore users 목록 불러오기
  const snapshot = await admin.firestore().collection("users").get();
  const users = snapshot.docs.map((doc) => doc.id);

logger.info("senderId =", senderId);
logger.info("all user ids =", users);
const senderDoc = await admin.firestore().doc(`users/${senderId}`).get();
logger.info("sender users/{uid} exists? =", senderDoc.exists);
logger.info("sender users/{uid} data =", senderDoc.data());
logger.info("users snapshot size =", snapshot.size);


  // 자기 자신 제외
  const candidates = users.filter((id) => id !== senderId);

  if (candidates.length === 0) {
    throw new HttpsError("failed-precondition", "랜덤 선택 대상이 없습니다.");
  }

  // 랜덤 선택
  const randomId =
    candidates[Math.floor(Math.random() * candidates.length)];

  return { receiverId: randomId };
});
