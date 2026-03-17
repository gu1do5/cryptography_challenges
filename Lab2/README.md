# Lab 2 – Secure Real-Time Media Streaming

## Challenge 1 – AES-GCM
```bash
cd .../hjUDPproxy_AES-GCM
java hjUDPproxy.java

cd .../hjStreamServer_AES-GCM
java hjStreamServer.java movies/*.dat localhost 8888

# Open VLC → udp://@:7777
```

## Challenge 2 – ChaCha20-Poly1305
```bash
cd .../hjUDPproxy_ChaCha20
java hjUDPproxy.java

cd .../hjStreamServer_ChaCha20
java hjStreamServer.java ../hjStreamServer_AES-GCM/movies/*.dat localhost 8888

# Open VLC → udp://@:7777
```

## Challenge 3 – DPRG Stream Cipher
```bash
cd .../hjUDPproxy_DPRG
java hjUDPproxy.java

cd .../hjStreamServer_DPRG
java hjStreamServer.java ../hjStreamServer_AES-GCM/movies/*.dat localhost 8888

# Open VLC → udp://@:7777
```
