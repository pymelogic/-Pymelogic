class BarcodeScanner {
    constructor(videoSelector) {
        this.videoSelector = videoSelector || '#interactive.viewport > video';
        this.isInitialized = false;
        this.onDetectedCallback = null;
        this.isUsingFrontCamera = false;
    }

    init() {
        var self = this;
        if (self.isInitialized) return Promise.resolve();

        return new Promise(function(resolve, reject) {
            navigator.mediaDevices.enumerateDevices()
                .then(function(devices) {
                    var cameras = devices.filter(function(device) {
                        return device.kind === 'videoinput';
                    });
                    
                    if (cameras.length === 0) {
                        reject(new Error('No se encontró ninguna cámara'));
                        return;
                    }

                    // Configuración para móviles
                    var config = {
                        inputStream: {
                            name: "Live",
                            type: "LiveStream",
                            target: document.querySelector(self.videoSelector),
                            constraints: {
                                facingMode: { ideal: 'environment' },
                                aspectRatio: { ideal: 1.777778 },
                                focusMode: 'continuous'
                            },
                            area: {
                                top: "20%",
                                right: "10%",
                                left: "10%",
                                bottom: "20%"
                            }
                        },
                        decoder: {
                            readers: [
                                "ean_reader",
                                "ean_8_reader",
                                "code_128_reader",
                                "code_39_reader",
                                "upc_reader",
                                "upc_e_reader"
                            ],
                            multiple: false,
                            debug: false,
                            tryHarder: true
                        },
                        locate: true,
                        locator: {
                            patchSize: "medium",
                            halfSample: true,
                            debug: false
                        }
                    };

                    Quagga.init(config, function(err) {
                        if (err) {
                            reject(err);
                            return;
                        }

                        console.log('Scanner inicializado correctamente');
                        self.isInitialized = true;
                        
                        // Configurar eventos
                        Quagga.onDetected(self._onDetected.bind(self));
                        Quagga.onProcessed(self._onProcessed.bind(self));
                        
                        resolve();
                    });
                })
                .catch(reject);
        });
    }

    start() {
        if (!this.isInitialized) {
            throw new Error('Scanner no inicializado. Llama a init() primero.');
        }
        Quagga.start();
    }

    stop() {
        if (this.isInitialized) {
            Quagga.stop();
            this.isInitialized = false;
        }
    }

    onDetected(callback) {
        this.onDetectedCallback = callback;
    }

    _onDetected(result) {
        var self = this;
        if (!result.codeResult) return;

        var code = result.codeResult.code;
        if (this._validateBarcode(code)) {
            // Reproducir feedback
            this._playBeepSound();
            
            // Notificar el código detectado
            if (this.onDetectedCallback) {
                this.onDetectedCallback(code);
            }
        }
    }

    _validateBarcode(code) {
        return code && code.length >= 8 && /^\d+$/.test(code);
    }

    _onProcessed(result) {
        var drawingCtx = Quagga.canvas.ctx.overlay;
        var drawingCanvas = Quagga.canvas.dom.overlay;

        if (result) {
            if (result.boxes) {
                drawingCtx.clearRect(0, 0, parseInt(drawingCanvas.getAttribute("width")), parseInt(drawingCanvas.getAttribute("height")));
                result.boxes.filter(function(box) {
                    return box !== result.box;
                }).forEach(function(box) {
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

    _playBeepSound() {
        // Vibrar en dispositivos móviles
        if (navigator.vibrate) {
            navigator.vibrate(200);
        }
    }

    switchCamera() {
        var self = this;
        return navigator.mediaDevices.enumerateDevices()
            .then(function(devices) {
                var cameras = devices.filter(function(device) {
                    return device.kind === 'videoinput';
                });
                
                if (cameras.length > 1) {
                    // Detener la cámara actual
                    self.stop();
                    
                    // Cambiar la configuración de la cámara
                    self.isUsingFrontCamera = !self.isUsingFrontCamera;
                    
                    // Reiniciar con la nueva configuración
                    return self.init()
                        .then(function() {
                            self.start();
                        });
                } else {
                    alert('Este dispositivo solo tiene una cámara');
                }
            })
            .catch(function(err) {
                console.error('Error al cambiar de cámara:', err);
                alert('Error al cambiar de cámara');
            });
    }
}
