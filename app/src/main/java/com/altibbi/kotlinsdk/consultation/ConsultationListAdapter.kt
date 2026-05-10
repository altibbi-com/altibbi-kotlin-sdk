package com.altibbi.kotlinsdk.consultation

import com.altibbi.kotlinsdk.R
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import com.altibbi.telehealth.model.Consultation
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class ConsultationListAdapter(
    private val onViewDetails: (consultationId: Int) -> Unit,
    private val onDeleteConsultation: (consultationId: Int) -> Unit,
    private val onShowMedia: (url: String) -> Unit,
    private val onDeleteMedia: (mediaId: String) -> Unit,
) : RecyclerView.Adapter<ConsultationListAdapter.Vh>() {

    private val items = mutableListOf<Consultation>()

    fun submitList(list: List<Consultation>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_consultation, parent, false)
        return Vh(view)
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.bind(items[position])
    }

    inner class Vh(private val root: View) : RecyclerView.ViewHolder(root) {

        fun bind(item: Consultation) {
            val ctx = root.context
            val density = ctx.resources.displayMetrics.density

            val statusRaw = item.status.orEmpty()
            val badge = root.findViewById<TextView>(R.id.item_status_badge)
            badge.text = if (statusRaw.isBlank()) "—" else statusRaw.uppercase(Locale.getDefault())
            val c = statusColor(ctx, statusRaw)
            badge.setTextColor(c)
            val gd = GradientDrawable()
            gd.cornerRadius = 12f * density
            gd.setColor(ColorUtils.setAlphaComponent(c, 0x33))
            badge.background = gd

            root.findViewById<TextView>(R.id.item_action_details).setOnClickListener {
                item.id?.let(onViewDetails)
            }
            root.findViewById<TextView>(R.id.item_action_delete).setOnClickListener {
                item.id?.let(onDeleteConsultation)
            }

            root.findViewById<TextView>(R.id.item_doctor_name).text =
                item.doctorName?.takeIf { it.isNotBlank() } ?: "Generic Doctor"
            root.findViewById<TextView>(R.id.item_question).text = item.question.orEmpty()

            val media = item.media.orEmpty()
            val mediaSection = root.findViewById<LinearLayout>(R.id.item_media_section)
            val mediaContainer = root.findViewById<LinearLayout>(R.id.item_media_container)
            if (media.isEmpty()) {
                mediaSection.visibility = View.GONE
            } else {
                mediaSection.visibility = View.VISIBLE
                root.findViewById<TextView>(R.id.item_media_title).text =
                    ctx.getString(R.string.consultation_media_title, media.size)
                mediaContainer.removeAllViews()
                val inflater = LayoutInflater.from(ctx)
                media.forEachIndexed { index, m ->
                    val row = inflater.inflate(R.layout.item_consultation_media_row, mediaContainer, false)
                    row.findViewById<TextView>(R.id.media_row_id).text =
                        ctx.getString(R.string.consultation_media_id_line, m.id ?: "—")
                    row.findViewById<TextView>(R.id.media_row_show).setOnClickListener {
                        onShowMedia(m.url.orEmpty())
                    }
                    row.findViewById<TextView>(R.id.media_row_remove).setOnClickListener {
                        val mid = m.id
                        if (!mid.isNullOrBlank()) onDeleteMedia(mid)
                    }
                    row.findViewById<View>(R.id.media_row_divider).visibility =
                        if (index == media.lastIndex) View.GONE else View.VISIBLE
                    mediaContainer.addView(row)
                }
            }

            root.findViewById<TextView>(R.id.item_footer_id).text = item.id?.toString() ?: "—"
            root.findViewById<TextView>(R.id.item_footer_medium).text = item.medium ?: "N/A"
            root.findViewById<TextView>(R.id.item_footer_date).text = formatDate(ctx, item.createdAt)
        }
    }

    private fun statusColor(ctx: Context, status: String): Int = when (status.lowercase(Locale.getDefault())) {
        "closed" -> ContextCompat.getColor(ctx, R.color.gray)
        "in_progress" -> ContextCompat.getColor(ctx, R.color.primary)
        "new" -> ContextCompat.getColor(ctx, R.color.secondary)
        else -> ContextCompat.getColor(ctx, R.color.text_primary)
    }

    private fun formatDate(ctx: Context, raw: String?): String {
        if (raw.isNullOrBlank()) return "N/A"
        return try {
            val instant = Instant.parse(raw)
            val formatter = DateTimeFormatter.ofLocalizedDate(java.time.format.FormatStyle.SHORT)
                .withZone(ZoneId.systemDefault())
            formatter.format(instant)
        } catch (_: Exception) {
            raw
        }
    }
}
