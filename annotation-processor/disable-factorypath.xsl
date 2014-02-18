<xsl:stylesheet version="1.0" 
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output omit-xml-declaration="yes" indent="yes"/>

    <xsl:param name="pNewType" select="'false'"/>

    <xsl:template match="node()|@*">
        <xsl:copy>
            <xsl:apply-templates select="node()|@*"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="factorypathentry/@enabled">
        <xsl:attribute name="enabled">
            <xsl:value-of select="$pNewType"/>
        </xsl:attribute>
    </xsl:template>
</xsl:stylesheet>