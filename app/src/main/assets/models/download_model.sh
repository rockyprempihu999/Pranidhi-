#!/bin/bash
echo "[+] Downloading MobileBERT model..."
wget --no-check-certificate 'https://drive.google.com/uc?export=download&id=1N5PBdmgKN6oi0X4c7qDCax2GNd00iV0s' -O intent_mobilebert_int8.tflite
echo "[✓] Model downloaded successfully!"
