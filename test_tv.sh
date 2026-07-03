curl -s "https://scanner.tradingview.com/egypt/scan" \
-H 'Content-Type: application/x-www-form-urlencoded' \
-d '{"symbols":{"tickers":["EGX:COMI","EGX:FWRY","EGX:EAST","EGX:TMGH","EGX:ABUK","EGX:SWDY"]},"columns":["close","change","volume","Recommend.All"]}'
