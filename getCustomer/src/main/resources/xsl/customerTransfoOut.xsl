<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:in="urn:org.redhat.integration.cd.customer"
	xmlns:out="http://backendSystem/target/">
	
	
	<xsl:template match="out:getCustomerInfoResponse" xmlns:out="http://backendSystem/target/">

		<in:getCustomerResponse xmlns:in="urn:org.redhat.integration.cd.customer">
			<in:body>
				<in:customer>
					<in:name>
						<xsl:value-of select="out:name" xmlns:out="http://backendSystem/target/"/>
					</in:name>
					<in:surname>
						<xsl:value-of select="out:lastname" xmlns:out="http://backendSystem/target/" />
					</in:surname>
					<in:status>
						<xsl:value-of select="out:code" xmlns:out="http://backendSystem/target/"/>
					</in:status>
				</in:customer>
			</in:body>
		</in:getCustomerResponse>

	</xsl:template>

</xsl:stylesheet>


