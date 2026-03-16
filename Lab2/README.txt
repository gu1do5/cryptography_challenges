Challenge 1
        cd .../hjUDPproxy
        java hjUDPproxy

        cd .../hjStreamServer
        java hjStreamServer

        open VLC
        udp://@:7777

Challenge 2
        cd .../hjUDPproxy_ChaCha20
        java hjUDPproxy

        cd .../hjStreamServer_ChaCha20
        java hjStreamServer ../hjStreamServer/movies/cars.dat localhost 7777

        open VLC
        udp://@:7777

Challenge 3
        cd .../hjUDPproxy_DPRG
        java hjUDPproxy

        cd .../hjStreamServer_DPRG
        java hjStreamServer ../hjStreamServer/movies/cars.dat localhost 8888

        open VLC
        udp://@:7777