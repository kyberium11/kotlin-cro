// ABBYY Mobile Capture © 2020 ABBYY Development, Inc.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.rtr.ui.sample.datacapture.profile_list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.abbyy.rtr.ui.sample.datacapture.data.Profile
import com.abbyy.rtr.ui.sample.datacapture.R

class ProfileData(
    @DrawableRes val iconRes: Int,
    @StringRes val titleRes: Int,
    val profile: Profile
)

class ProfileAdapter(
    private val profiles: List<ProfileData>,
    private val listener: Listener
) : RecyclerView.Adapter<ProfileViewHolder>() {
    interface Listener {
        fun onProfileClicked(profile: Profile)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.list_item_profile, parent, false)
        return ProfileViewHolder(view, listener)
    }

    override fun getItemCount() = profiles.size

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        holder.bind(profiles[position])
    }
}

class ProfileViewHolder(view: View, private val listener: ProfileAdapter.Listener) :
    RecyclerView.ViewHolder(view) {
    init {
        view.setOnClickListener {
            listener.onProfileClicked(profile)
        }
    }

    private val imageView: ImageView = view.findViewById(R.id.profileImageView)
    private val textView: TextView = view.findViewById(R.id.profileTextView)
    private lateinit var profile: Profile

    fun bind(profileData: ProfileData) {
        imageView.setImageResource(profileData.iconRes)
        textView.setText(profileData.titleRes)
        profile = profileData.profile
    }
}
