// ABBYY Mobile Capture © 2020 ABBYY Development, Inc.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.rtr.ui.sample.datacapture

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abbyy.rtr.ui.sample.datacapture.data.Profile
import com.abbyy.rtr.ui.sample.datacapture.profile_list.ProfileAdapter
import com.abbyy.rtr.ui.sample.datacapture.profile_list.ProfileData

class ProfileListFragment : Fragment(R.layout.fragment_profiles), ProfileAdapter.Listener {
    companion object {
        fun newInstance() = ProfileListFragment()

        private val profileDataList = listOf(
            ProfileData(
                R.drawable.ic_text,
                R.string.text,
                Profile.TEXT
            ),
//            ProfileData(
//                R.drawable.ic_business_card,
//                R.string.business_card,
//                Profile.BUSINESS_CARD
//            ),
//            ProfileData(
//                R.drawable.ic_iban,
//                R.string.iban,
//                Profile.IBAN
//            ),
//            ProfileData(
//                R.drawable.ic_invoice,
//                R.string.invoice,
//                Profile.INVOICE
//            )
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.profileRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)
        val adapter = ProfileAdapter(profileDataList, this)
        recyclerView.adapter = adapter

        val buildNumberView = view.findViewById<TextView>(R.id.buildNumber)
        buildNumberView.text = getString(R.string.build_number, BuildConfig.VERSION_NAME)
    }

    override fun onProfileClicked(profile: Profile) {
        (requireActivity() as AppActivity).openDataCaptureScreen(profile)
    }
}
