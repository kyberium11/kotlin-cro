// ABBYY Mobile Capture © 2020 ABBYY Development, Inc.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.rtr.ui.sample.datacapture.capture

import com.abbyy.mobile.uicomponents.scenario.DataCaptureScenario
import com.abbyy.mobile.uicomponents.scenario.DataCaptureScenario.DataField
import com.abbyy.mobile.uicomponents.scenario.DataCaptureScenario.ResourceType
import com.abbyy.mobile.uicomponents.scenario.DataCaptureScenario.ResourceType.START_TIP
import com.abbyy.mobile.uicomponents.scenario.DataCaptureScenario.ResourceType.LOOKING_FOR_DOCUMENT_TIP
import com.abbyy.mobile.uicomponents.scenario.DataCaptureScenario.ResourceType.COMPLETE_TIP
import com.abbyy.mobile.uicomponents.scenario.DataCaptureScenario.ResourceType.CROP_DOCUMENT_TIP
import com.abbyy.rtr.ui.sample.datacapture.R
import com.abbyy.rtr.ui.sample.datacapture.data.*

// The configurations for various data capture scenarios

class DataCaptureConfig(
    val profileName: String? = null,
    val customProfileScheme: DataCaptureScenario.DataCaptureSettings.Scheme? = null,
    val resourceTypeToStringRes: Map<ResourceType, Int>,
    val areaOfInterest: DataCaptureScenario.AreaOfInterest? = null,
    val isGalleryCaptureVisible: Boolean = true,
    val shouldUserClickToStart: Boolean = false,
    val fieldsConverter: (Array<DataField>) -> List<ProfileField>
)

fun getConfigs(profile: Profile): List<DataCaptureConfig> {
    return when (profile) {
        Profile.TEXT -> getTextConfigs()
//        Profile.BUSINESS_CARD -> getBusinessCardConfigs()
//        Profile.IBAN -> getIbanConfigs()
//        Profile.INVOICE -> getInvoiceConfigs()
    }
}

private fun getInvoiceConfigs(): List<DataCaptureConfig> {
    return listOf(
        // Invoice number
        DataCaptureConfig(
            customProfileScheme = DataCaptureScenario.DataCaptureSettings.Scheme(
                "invoice",
                DataCaptureScenario.DataCaptureSettings.Scheme.Field(
                    INVOICE_NUMBER,
                    "[A-Za-z0-9-]{5,20}"
                )
            ),
            resourceTypeToStringRes = mapOf(
                START_TIP to R.string.invoice_number_start_tip,
                LOOKING_FOR_DOCUMENT_TIP to R.string.invoice_number_move_inside_area_of_interest,
                COMPLETE_TIP to R.string.invoice_number_complete_tip
            ),
            isGalleryCaptureVisible = false,
            areaOfInterest = DataCaptureScenario.AreaOfInterest.ONE_LINE,
            fieldsConverter = ::convertFieldList
        ),
        // Vendor name
        DataCaptureConfig(
            profileName = "Text",
            resourceTypeToStringRes = mapOf(
                START_TIP to R.string.invoice_vendor_name_start_tip,
                LOOKING_FOR_DOCUMENT_TIP to R.string.invoice_vendor_name_move_inside_area_of_interest,
                COMPLETE_TIP to R.string.invoice_vendor_name_complete_tip
            ),
            isGalleryCaptureVisible = false,
            areaOfInterest = DataCaptureScenario.AreaOfInterest.withAspectRatio(3f),
            fieldsConverter = ::convertInvoiceVendorNameFieldList
        ),
        // Total
        DataCaptureConfig(
            customProfileScheme = DataCaptureScenario.DataCaptureSettings.Scheme(
                "invoice",
                DataCaptureScenario.DataCaptureSettings.Scheme.Field(
                    INVOICE_TOTAL,
                    "[0-9]+[\\.,]?[0-9]*"
                )
            ),
            resourceTypeToStringRes = mapOf(
                START_TIP to R.string.invoice_total_start_tip,
                LOOKING_FOR_DOCUMENT_TIP to R.string.invoice_total_move_inside_area_of_interest
            ),
            isGalleryCaptureVisible = false,
            areaOfInterest = DataCaptureScenario.AreaOfInterest.ONE_LINE,
            fieldsConverter = ::convertFieldList
        )
    )
}

private fun convertInvoiceVendorNameFieldList(fields: Array<DataField>): List<ProfileField> {
    val vendorName = fields
        .mapNotNull(DataField::text)
        .joinToString(separator = "\n")

    return listOf(ProfileField(INVOICE_VENDOR_NAME, vendorName))
}

private fun getIbanConfigs(): List<DataCaptureConfig> {
    return listOf(
        DataCaptureConfig(
            profileName = "IBAN",
            resourceTypeToStringRes = mapOf(
                START_TIP to R.string.iban_start_tip,
                LOOKING_FOR_DOCUMENT_TIP to R.string.iban_move_inside_area_of_interest
            ),
            fieldsConverter = ::convertFieldList
        )
    )
}

private fun getTextConfigs(): List<DataCaptureConfig> {
    return listOf(
        DataCaptureConfig(
            profileName = "Text",
            resourceTypeToStringRes = mapOf(
                START_TIP to R.string.text_start_tip,
                LOOKING_FOR_DOCUMENT_TIP to R.string.text_move_inside_area_of_interest
            ),
            fieldsConverter = ::convertTextFieldList
        )
    )
}

private fun convertFieldList(fields: Array<DataField>): List<ProfileField> {
    return fields.mapNotNull(::convertField)
}

private fun convertTextFieldList(fields: Array<DataField>): List<ProfileField> {
    val strings = fields.mapNotNull(DataField::text)
    return listOf(ProfileField(TEXT, strings.joinToString(separator = "\n")))
}

private fun getBusinessCardConfigs(): List<DataCaptureConfig> {
    return listOf(
        DataCaptureConfig(
            profileName = "BusinessCards",
            resourceTypeToStringRes = mapOf(
                START_TIP to R.string.business_card_start_tip,
                LOOKING_FOR_DOCUMENT_TIP to R.string.business_card_move_inside_area_of_interest,
                CROP_DOCUMENT_TIP to R.string.business_card_crop_tip
            ),
            fieldsConverter = ::convertBusinessCardFieldList
        )
    )
}

private fun convertBusinessCardFieldList(fields: Array<DataField>): List<ProfileField> {
    return fields.flatMap(::convertBusinessCardField)
}

private fun convertBusinessCardField(field: DataField): List<ProfileField> =
    when (field.id) {
        null -> emptyList()
        // nested fields
        BC_ADDRESS, BC_FULLNAME, BC_JOB -> field.components?.flatMap(::convertBusinessCardField)
            ?: emptyList()
        // leaf field
        else -> convertField(field)?.let { converted -> listOf(converted) } ?: emptyList()
    }

private fun convertField(field: DataField): ProfileField? {
    val id = field.id ?: return null
    val text = field.text ?: return null
    return ProfileField(id, text)
}