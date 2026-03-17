INSTRUCTIONS

Challenge 1
        cd .../hjUDPproxy_AES-GCM
        java hjUDPproxy.java

        cd .../hjStreamServer_AES-GCM
        java hjStreamServer.java ../hjStreamServer/movies/*.dat localhost 8888

        open VLC
        udp://@:7777

Challenge 2
        cd .../hjUDPproxy_ChaCha20
        java hjUDPproxy.java

        cd .../hjStreamServer_ChaCha20
        java hjStreamServer.java ../hjStreamServer/movies/*.dat localhost 8888

        open VLC
        udp://@:7777

Challenge 3
        cd .../hjUDPproxy_DPRG
        java hjUDPproxy.java

        cd .../hjStreamServer_DPRG
        java hjStreamServer.java ../hjStreamServer/movies/*.dat localhost 8888

        open VLC
        udp://@:7777