function () {

    const pollAndResolveForData = (attempts, resolve, reject) => {
        setTimeout(() => {
          if (window.webauthnResolution != null) {
            console.log("got data, now resolving:");
            console.log(window.webauthnResolution);

            resolve(window.webauthnResolution);
          } else {
            if (attempts > 15) {
              reject("didnt receive response");
            } else {
              pollAndResolveForData(attempts + 1, resolve, reject);
            }
          }
        }, 500);
      };

    class CredentialsContainer {

        create(opts) {
            window.webauthnResolution = null;
            const jsonMessage = JSON.stringify(opts);
            webAuthnInterface.publicKeyCredentialCreate(jsonMessage);

            return new Promise(function(resolve, reject) {
                 pollAndResolveForData(0, resolve, reject);
            });
        };

        get(opts) {
            window.webauthnResolution = null;
            const jsonMessage = JSON.stringify(opts);
            webAuthnInterface.publicKeyCredentialGet(jsonMessage);

            return new Promise(function(resolve, reject) {
                pollAndResolveForData(0, resolve, reject);
            });
        };
    };

    credentialsContainer = new CredentialsContainer();

    navigator.credentials = credentialsContainer;

    navigator.credentials.create = credentialsContainer.create;
    navigator.credentials.get = credentialsContainer.get;

    window.PublicKeyCredential = function() {
    }

    window.PublicKeyCredential.isUserVerifyingPlatformAuthenticatorAvailable = function() {
    }

    console.log(PublicKeyCredential);
    console.log(PublicKeyCredential.isUserVerifyingPlatformAuthenticatorAvailable);
}