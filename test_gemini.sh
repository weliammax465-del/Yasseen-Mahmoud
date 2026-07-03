curl -s -w "%{http_code}" "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$GEMINI_API_KEY" \
-H 'Content-Type: application/json' \
-d '{
  "contents": [
    {"parts": [{"text": "What is the stock price of Apple?"}]}
  ],
  "tools": [{"googleSearchRetrieval": {}}]
}'
