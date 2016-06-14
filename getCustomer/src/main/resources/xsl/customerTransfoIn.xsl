<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:in="urn:org.redhat.integration.cd.customer"
	xmlns:out="http://backendSystem/target/">
	
	<xsl:param name="CUSTOMER_ID"/>
	
	<xsl:template match="in:customerId">

		<out:getCustomerInfoRequest xmlns:out="http://backendSystem/target/">
			<out:externalClientNo>
				<xsl:value-of select="$CUSTOMER_ID" />
			</out:externalClientNo>
		</out:getCustomerInfoRequest>

	</xsl:template>

</xsl:stylesheet>


