# fabric-cd
Continuous delivery with Fuse Fabric

#  Definition
Continuous delivery is a process that provides the ability to deliver deployable binaries to an environment continuously.
It means that any binary produced by the process is a candidate for a deployment to the target environment, which can be UAT but usually is production
The process should never produce binaries which are not stable enough ot be a candidate for a deployment

# Example description
The example is an ESB simple application named getCustomer
It exposes a soap-based integration web-service.
This service receives a soap message containing a customer ID, applies an XSLT transformation to it, calls a backend service named CustomerService that returns details about the customer (name, lastname...), applies another XSLT transformation to the response before sending it back to the client application.
The application is a Camel route using CXF endpoints, configured to be deployed on a Fuse Fabric.

 
