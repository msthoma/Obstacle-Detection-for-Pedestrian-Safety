package cy.org.rise.obsai.ui

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import cy.org.rise.obsai.R
import cy.org.rise.obsai.db.Obstacle
import cy.org.rise.obsai.utils.Constants
import cy.org.rise.obsai.utils.TAG
import cy.org.rise.obsai.utils.roundTo
import kotlinx.android.synthetic.main.row_item.view.*
import java.io.File

/** Provides views to the RecyclerView with obstacle data. */
class CustomAdapter internal constructor() :
    RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    private var obstacles = emptyList<Obstacle>() // Cached copy of obstacles

    /** Provide a reference to the type of views that you are using (custom ViewHolder) */
    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val locationView: TextView
        val timeView: TextView
        val typeView: TextView
        val uploadStatusView: ImageView

        init {
            v.apply {
                setOnClickListener { Log.d(TAG(), "Element $adapterPosition clicked.") }
                locationView = location_view
                timeView = time_view
                typeView = type_view
                uploadStatusView = upload_status_view
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view.
        val v = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.row_item, viewGroup, false)
        return ViewHolder(v)
    }

    // Replace the contents of a view (invoked by the layout manager)
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        // Get element from the dataset at this position and replace the contents of the view
        // with that element
        val obs = obstacles[position]

        viewHolder.apply {
            typeView.text = "Type: ${obs.obstacleType}"
            locationView.text =
                "Location: ${obs.location.latitude.roundTo(5)}, ${obs.location.longitude.roundTo(5)}"
            // TODO 11/07/20 format dates to locale strings, add ThreeTenABP
            timeView.text = obs.timeStamp.toString()
        }

        when (obs.uploadStatus) {
            Constants.UPLOAD_SUCCESS -> viewHolder.uploadStatusView.apply {
                setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.ic_cloud_done_outline_24dp,
                        null
                    )
                )
                setColorFilter(ResourcesCompat.getColor(resources, R.color.uploadSuccess, null))
            }
            Constants.UPLOAD_FAIL -> viewHolder.uploadStatusView.apply {
                setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.ic_error_outline_24dp,
                        null
                    )
                )
                setColorFilter(ResourcesCompat.getColor(resources, R.color.uploadError, null))
            }
            Constants.UPLOADING -> viewHolder.uploadStatusView.apply {
                setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.ic_uploading_outline_24dp,
                        null
                    )
                )
                setColorFilter(ResourcesCompat.getColor(resources, R.color.uploadInProgress, null))
            }
        }

        // Set picture
        Picasso.get()
            .load(File(obs.photoPath))
            .placeholder(R.drawable.ic_noun_barrier_2895012)
            .resize(200, 0)
            .centerInside()
            .into(viewHolder.itemView.imageView)
    }

    internal fun setObstacles(obstacles: List<Obstacle>) {
        this.obstacles = obstacles
        notifyDataSetChanged()
    }

    /**
     * Return the size of your dataset (invoked by the layout manager)
     */
    override fun getItemCount() = obstacles.size
}
