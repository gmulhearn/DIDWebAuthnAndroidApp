function () {
    class CredentialsContainer {

        create(opts) {

            const webAuthnContext = window.webAuthnContext;

            const jsonMessage = JSON.stringify(opts);
            webAuthnInterface.publicKeyCredentialCreate(jsonMessage);

            return new Promise(function(resolve, reject) {
                 setTimeout(() => {
                     if (webAuthnContext.authRegistrationResponse != null) {
                         resolve(webAuthnContext.authRegistrationResponse);
                     }
                 }, 2000);
            });
        };

        get(opts) {
            const webAuthnContext = window.webAuthnContext;

            const jsonMessage = JSON.stringify(opts);
            webAuthnInterface.publicKeyCredentialGet(jsonMessage);
            return new Promise(function(resolve, reject) {
                setTimeout(() => {
                    if (webAuthnContext.authAuthenticationResponse != null) {
                        resolve(webAuthnContext.authAuthenticationResponse);
                    }
                }, 1000);
            });
        };
        
        resolveRegistration(response) {
            const webAuthnContext = window.webAuthnContext;

            console.log(response);
            console.log(webAuthnContext.authRegistrationResponse);
            webAuthnContext.authRegistrationResponse = JSON.parse(JSON.stringify(response));
        }

        resolveAuthentication(response) {
            const webAuthnContext = window.webAuthnContext;

            webAuthnContext.authAuthenticationResponse = JSON.parse(JSON.stringify(response));
        }
    };

    const webAuthnContext = window.webAuthnContext = {};

    credentialsContainer = new CredentialsContainer();

    navigator.credentials.create = credentialsContainer.create;
    navigator.credentials.get = credentialsContainer.get;
    navigator.credentials.resolveRegistration = credentialsContainer.resolveRegistration;
    navigator.credentials.resolveAuthentication = credentialsContainer.resolveAuthentication;

    window.PublicKeyCredential = function() {
    }
}