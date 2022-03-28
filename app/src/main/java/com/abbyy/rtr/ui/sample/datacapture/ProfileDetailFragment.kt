// ABBYY Mobile Capture © 2020 ABBYY Development, Inc.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.rtr.ui.sample.datacapture

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.abbyy.rtr.ui.sample.datacapture.data.*
import com.abbyy.rtr.ui.sample.datacapture.profile_detail.FieldAdapter
import com.abbyy.rtr.ui.sample.datacapture.profile_detail.Item
import kotlinx.android.synthetic.main.fragment_profile_detail.*
import java.util.*
import java.util.concurrent.TimeUnit
import android.util.Log

class ProfileDetailFragment :
    Fragment(R.layout.fragment_profile_detail), FieldAdapter.Listener, Handler.Callback {

    companion object {
        fun newInstance() = ProfileDetailFragment()
    }

    private lateinit var storage: ProfileResultStorage
    private lateinit var profile: Profile
    private val handler = Handler(Looper.getMainLooper(), this)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDependencies()
        setupViews()
        loadResult()
    }

    private fun setupDependencies() {
        storage = App.profileResultStorage
    }

    private fun setupViews() {
        backButton.setOnClickListener { requireFragmentManager().popBackStack() }
        retakeImageButton.setOnClickListener {
            val appActivity = requireActivity() as AppActivity
            appActivity.dismissKeyboard()
            appActivity.openRetakeImageScreen()
        }
        shareButton.setOnClickListener { shareFields() }
        rescanButton.setOnClickListener {
            val appActivity = requireActivity() as AppActivity
            appActivity.dismissKeyboard()
            appActivity.openDataCaptureScreen(profile)
        }
    }

    private fun shareFields() {
        lifecycleScope.launchWhenStarted {
            val fields = storage.loadFields() ?: return@launchWhenStarted
            val text =
                fields.joinToString(separator = "\n") { field -> "${field.id}: ${field.value}" }

            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, text)
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }
    }

    private fun loadResult() {
        lifecycleScope.launchWhenStarted {
            val result = storage.load() ?: return@launchWhenStarted
            profile = result.profile
            fieldsRecyclerView.setHasFixedSize(true)
            fieldsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            val items = getAdapterItems(result.fields)
            val adapter = FieldAdapter(items, this@ProfileDetailFragment)
            fieldsRecyclerView.adapter = adapter
            retakeImageButton.isVisible = result.image != null
            profileImageView.isVisible = result.image != null
            profileImageView.setImageBitmap(result.image)
        }
    }

    private fun getAdapterItems(fields: List<ProfileField>): List<Item> {
        val result = LinkedList(fields.map { field ->
            Item(
                field = field,
                // A text field usually takes several lines
                isSingleLine = isFieldSingleLine(field.id),
                titleStringRes = getFieldTitleStringRes(field.id)
            )
        })

        arrayOf(
            BC_FULLNAME_FIRSTNAME,
            BC_FULLNAME_LASTNAME,
            BC_JOB_POSITION,
            BC_COMPANY
        ).reversedArray().forEach { name ->
            result.firstOrNull { item -> item.field.id == name }?.let { item ->
                result.remove(item)
                result.addFirst(item)
            }
        }

        return result
    }

    private fun isFieldSingleLine(id: String) = when (id) {
        TEXT -> false
        INVOICE_VENDOR_NAME -> false
        else -> true
    }

    @StringRes
    private fun getFieldTitleStringRes(fieldId: String) = when (fieldId) {
        TEXT -> R.string.text_title
        INVOICE_NUMBER -> R.string.invoice_number_title
        INVOICE_VENDOR_NAME -> R.string.invoice_vendor_name_title
        INVOICE_TOTAL -> R.string.invoice_total_title
        IBAN -> R.string.iban_title
        BC_ADDRESS_CITY -> R.string.bc_city_title
        BC_ADDRESS_COUNTRY -> R.string.bc_country_title
        BC_ADDRESS_REGION -> R.string.bc_region_title
        BC_ADDRESS_STREET_ADDRESS -> R.string.bc_street_title
        BC_ADDRESS_ZIP_CODE -> R.string.bc_zip_title
        BC_COMPANY -> R.string.bc_company_title
        BC_EMAIL -> R.string.bc_email_title
        BC_FAX -> R.string.bc_fax_title
        BC_FULLNAME_DEGREE -> R.string.bc_degree_title
        BC_FULLNAME_EXTRANAME -> R.string.bc_extra_name_title
        BC_FULLNAME_FIRSTNAME -> R.string.bc_first_name_title
        BC_FULLNAME_LASTNAME -> R.string.bc_last_name_title
        BC_FULLNAME_MIDDLENAME -> R.string.bc_middle_name_title
        BC_FULLNAME_TITLE -> R.string.bc_name_title_title
        BC_JOB_DEPARTMENT -> R.string.bc_job_department_title
        BC_JOB_POSITION -> R.string.bc_job_position_title
        BC_MOBILE -> R.string.bc_mobile_title
        BC_PHONE -> R.string.bc_phone_title
        BC_WEB -> R.string.bc_web_title
        else -> throw IllegalStateException("Unknown field id")
    }

    override fun onFieldChanged(position: Int, field: ProfileField) {
        // Throttle saving changes to disk
        handler.removeMessages(position)
        val message = handler.obtainMessage(position)
        message.obj = field
        handler.sendMessageDelayed(message, TimeUnit.MILLISECONDS.toMillis(500))
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    override fun handleMessage(msg: Message): Boolean {
        lifecycleScope.launchWhenStarted {
            val position = msg.what
            val field = msg.obj as ProfileField
            storage.updateField(position, field)
        }
        return true
    }
}
