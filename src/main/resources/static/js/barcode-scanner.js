class BarcodeScanner {
    constructor(videoSelector = '#interactive.viewport > video') {
        this.videoSelector = videoSelector;
        this.isInitialized = false;
        this.onDetectedCallback = null;
        this.currentStream = null;
    }

    async init() {
        if (this.isInitialized) return;

        try {
            // Verificar si hay cámara disponible
            const devices = await navigator.mediaDevices.enumerateDevices();
            const cameras = devices.filter(device => device.kind === 'videoinput');
            
            if (cameras.length === 0) {
                throw new Error('No se encontró ninguna cámara');
            }

            // Intentar obtener la cámara trasera primero
            const constraints = {
                audio: false,
                video: {
                    facingMode: { ideal: 'environment' },
                    width: { min: 640, ideal: 1280, max: 1920 },
                    height: { min: 480, ideal: 720, max: 1080 },
                    aspectRatio: { ideal: 1.777778 },
                    focusMode: 'continuous'
                }
            };

            // Guardar el stream para poder cambiarlo después
            try {
                this.currentStream = await navigator.mediaDevices.getUserMedia(constraints);
            } catch (error) {
                console.warn('No se pudo acceder a la cámara trasera, intentando con la frontal');
                constraints.video.facingMode = { ideal: 'user' };
                this.currentStream = await navigator.mediaDevices.getUserMedia(constraints);
            }

            // Configurar Quagga con optimizaciones de rendimiento
            await Quagga.init({
                inputStream: {
                    name: "Live",
                    type: "LiveStream",
                    target: document.querySelector(this.videoSelector),
                    constraints: constraints,
                    area: { // Reducir el área de escaneo para mejorar el rendimiento
                        top: "0%",
                        right: "0%",
                        left: "0%",
                        bottom: "0%"
                    }
                },
                frequency: 30, // Aumentar la frecuencia de escaneo
                decoder: {
                    readers: [
                        "ean_reader",
                        "ean_8_reader"
                    ],
                    multiple: false, // Solo buscar un código a la vez
                    debug: false, // Desactivar debug para mejor rendimiento
                    tryHarder: true // Intentar más fuerte en cada frame
                },
                locate: true,
                locator: {
                    patchSize: "x-large", // Usar parches más grandes para detección más rápida
                    halfSample: true,
                    debug: false
                },
                numOfWorkers: navigator.hardwareConcurrency || 4, // Usar múltiples workers
                decoder: {
                    readers: ["ean_reader", "ean_8_reader"],
                    multiple: false
                }
            });

            this.isInitialized = true;
            console.log('Scanner inicializado correctamente');
            
            // Eventos de detección
            Quagga.onDetected(this._onDetected.bind(this));
            Quagga.onProcessed(this._onProcessed.bind(this));

        } catch (error) {
            console.error('Error al inicializar el scanner:', error);
            throw error;
        }
    }

    start() {
        if (!this.isInitialized) {
            throw new Error('Scanner no inicializado. Llama a init() primero.');
        }
        Quagga.start();
    }

    stop() {
        Quagga.stop();
        this.isInitialized = false;
        if (this.currentStream) {
            this.currentStream.getTracks().forEach(track => track.stop());
            this.currentStream = null;
        }
    }

    onDetected(callback) {
        this.onDetectedCallback = callback;
    }

    // Variable para controlar el tiempo entre detecciones
    lastDetectionTime = 0;
    minimumDetectionInterval = 1000; // 1 segundo entre detecciones para móviles

    _onDetected(result) {
        const currentTime = Date.now();
        if (result.codeResult && 
            (currentTime - this.lastDetectionTime) > this.minimumDetectionInterval) {
            
            this.lastDetectionTime = currentTime;
            const code = result.codeResult.code;
            
            // Verificar que el código tenga un formato válido
            if (this._validateBarcode(code)) {
                // Detener el scanner temporalmente para evitar múltiples lecturas
                Quagga.pause();
                
                // Reproducir sonido y vibración de éxito
                this._playBeepSound();
                
                // Ejecutar callback si existe
                if (this.onDetectedCallback) {
                    this.onDetectedCallback(code);
                }

                // Reanudar después de un breve delay
                setTimeout(() => {
                    Quagga.start();
                }, 1500);
            }
        }
    }

    _validateBarcode(code) {
        // Validar que el código tenga un formato válido
        // Ajusta esta validación según tus necesidades
        return code && code.length >= 8 && /^\d+$/.test(code);
    }

    _onProcessed(result) {
        let drawingCtx = Quagga.canvas.ctx.overlay,
            drawingCanvas = Quagga.canvas.dom.overlay;

        if (result) {
            if (result.boxes) {
                drawingCtx.clearRect(0, 0, parseInt(drawingCanvas.getAttribute("width")), parseInt(drawingCanvas.getAttribute("height")));
                result.boxes.filter(function (box) {
                    return box !== result.box;
                }).forEach(function (box) {
                    Quagga.ImageDebug.drawPath(box, { x: 0, y: 1 }, drawingCtx, { color: "green", lineWidth: 2 });
                });
            }

            if (result.box) {
                Quagga.ImageDebug.drawPath(result.box, { x: 0, y: 1 }, drawingCtx, { color: "#00F", lineWidth: 2 });
            }

            if (result.codeResult && result.codeResult.code) {
                Quagga.ImageDebug.drawPath(result.line, { x: 'x', y: 'y' }, drawingCtx, { color: 'red', lineWidth: 3 });
            }
        }
    }

    async     _playBeepSound() {
        try {
            // Vibrar en dispositivos móviles
            if (navigator.vibrate) {
                navigator.vibrate(200);
            }
            
            // Reproducir un beep
            var context = new (window.AudioContext || window.webkitAudioContext)();
            var osc = context.createOscillator();
            var gain = context.createGain();
            
            osc.connect(gain);
            gain.connect(context.destination);
            
            osc.type = 'sine';
            osc.frequency.setValueAtTime(1000, context.currentTime);
            gain.gain.setValueAtTime(0.1, context.currentTime);
            
            osc.start(context.currentTime);
            
            setTimeout(function() {
                osc.stop();
                context.close();
            }, 100);
        } catch (error) {
            console.error('Error al reproducir el sonido:', error);
        }
    }

    // Método para cambiar entre cámaras
    switchCamera() {
        var self = this;
        
        if (self.currentStream) {
            self.currentStream.getTracks().forEach(function(track) {
                track.stop();
            });
        }

        return navigator.mediaDevices.enumerateDevices()
            .then(function(devices) {
                var videoDevices = devices.filter(function(device) {
                    return device.kind === 'videoinput';
                });
                
                if (videoDevices.length < 2) {
                    throw new Error('No hay suficientes cámaras disponibles');
                }

                // Obtener el ID de la cámara actual y cambiar a la siguiente
                var currentTrack = self.currentStream && self.currentStream.getVideoTracks()[0];
                var currentFacingMode = currentTrack && currentTrack.getSettings().facingMode;
                var newFacingMode = currentFacingMode === 'environment' ? 'user' : 'environment';
                
                self.stop();
                return self.init({ video: { facingMode: { ideal: newFacingMode } } })
                    .then(function() {
                        self.start();
                    });
            });
    }
        try {
            const audioContext = new (window.AudioContext || window.webkitAudioContext)();
            const oscillator = audioContext.createOscillator();
            const gainNode = audioContext.createGain();

            oscillator.connect(gainNode);
            gainNode.connect(audioContext.destination);

            oscillator.type = 'sine';
            oscillator.frequency.value = 1000;
            gainNode.gain.value = 0.1;

            oscillator.start();
            setTimeout(() => {
                oscillator.stop();
                audioContext.close();
            }, 100);
            
            // Vibrar en dispositivos móviles
            if (navigator.vibrate) {
                navigator.vibrate(200);
            }
        } catch (error) {
            console.error('Error al reproducir el sonido:', error);
        }
    }

    // Método para cambiar entre cámaras
    async switchCamera() {
        if (this.currentStream) {
            this.currentStream.getTracks().forEach(track => track.stop());
        }

        const devices = await navigator.mediaDevices.enumerateDevices();
        const videoDevices = devices.filter(device => device.kind === 'videoinput');
        
        if (videoDevices.length < 2) {
            throw new Error('No hay suficientes cámaras disponibles');
        }

        // Obtener el ID de la cámara actual
        const currentDevice = videoDevices.find(device => 
            this.currentStream && 
            this.currentStream.getVideoTracks()[0].getSettings().deviceId === device.deviceId
        );

        const nextDevice = videoDevices.find(device => device.deviceId !== currentDevice?.deviceId);
        
        if (nextDevice) {
            await this.stop();
            // Cambiar la orientación de la cámara
            const currentFacingMode = this.currentStream?.getVideoTracks()[0]?.getSettings()?.facingMode;
            const newFacingMode = currentFacingMode === 'environment' ? 'user' : 'environment';
            
            await this.init({ facingMode: { ideal: newFacingMode } });
            this.start();
        }
    }
        // Crear y reproducir un sonido de beep
        const audioContext = new (window.AudioContext || window.webkitAudioContext)();
        const oscillator = audioContext.createOscillator();
        const gainNode = audioContext.createGain();

        oscillator.connect(gainNode);
        gainNode.connect(audioContext.destination);

        oscillator.frequency.value = 800;
        gainNode.gain.value = 0.5;

        oscillator.start(audioContext.currentTime);
        oscillator.stop(audioContext.currentTime + 0.15);
    }
}
