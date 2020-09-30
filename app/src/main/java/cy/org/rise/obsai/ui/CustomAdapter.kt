package cy.org.rise.obsai.ui

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
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import java.io.File

/** Provides views to the RecyclerView with obstacle data. */
class CustomAdapter internal constructor() :
    RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    private val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
    private var obstacles = emptyList<Obstacle>() // Cached copy of obstacles

    /** Provide a reference to the type of views that you are using (custom ViewHolder) */
    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val locationView: TextView
        val obstaclePhotoView: ImageView
        val timeView: TextView
        val typeView: TextView
//        val uploadStatusView: ImageView

        init {
            v.apply {
                setOnClickListener { Log.d(TAG(), "Element $adapterPosition clicked.") }
                locationView = location_view
                obstaclePhotoView = obstacle_image_view
                timeView = time_view
                typeView = type_view
//                uploadStatusView = upload_status_view
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view
        val v = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.row_item, viewGroup, false)
        return ViewHolder(v)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        // Get element from dataset at this position and replace the contents with element details
        val obs = obstacles[position]

        // Get reference to resources, needed many times below
        val res = viewHolder.itemView.resources

        viewHolder.apply {
            typeView.text = res.getString(R.string.detail_object_type, obs.obstacleType)
            locationView.text = res.getString(
                R.string.detail_location,
                "${obs.location.latitude.roundTo(5)}, ${obs.location.longitude.roundTo(5)}"
            )
            timeView.text = Instant.ofEpochMilli(obs.timeStamp.time)
                .atZone(ZoneId.systemDefault()).toLocalDateTime().format(formatter)

            // Set upload status indicator
//            when (obs.uploadStatus) {
//                Constants.UPLOAD_SUCCESS -> uploadStatusView.apply {
//                    setImageDrawable(
//                        ResourcesCompat
//                            .getDrawable(res, R.drawable.ic_cloud_done_outline_24dp, null)
//                    )
//                    setColorFilter(ResourcesCompat.getColor(res, R.color.uploadSuccess, null))
//                }
//                Constants.UPLOAD_FAIL -> uploadStatusView.apply {
//                    setImageDrawable(
//                        ResourcesCompat.getDrawable(res, R.drawable.ic_error_outline_24dp, null)
//                    )
//                    setColorFilter(ResourcesCompat.getColor(res, R.color.uploadError, null))
//                }
//                Constants.UPLOADING -> uploadStatusView.apply {
//                    setImageDrawable(
//                        ResourcesCompat.getDrawable(res, R.drawable.ic_uploading_outline_24dp, null)
//                    )
//                    setColorFilter(ResourcesCompat.getColor(res, R.color.uploadInProgress, null))
//                }
//            }

            // Set picture
            Picasso.get()
                .load(File(obs.photoPath))
                .placeholder(R.drawable.ic_noun_barrier_2895012)
                .resize(200, 0)
                .centerInside()
                .into(obstaclePhotoView)

//            this.itemView.setOnClickListener {
//                Log.d(TAG("ORIGINAL"), "$obs")
//                val obsJSON = obs.toJson()
//                Log.d(TAG("JSON"), obsJSON)
//                Log.d(TAG("BACK to obs"), Gson().fromJson(obsJSON, Obstacle::class.java).toJson())
//            }
        }
    }

    internal fun setObstacles(obstacles: List<Obstacle>) {
        this.obstacles = obstacles
        notifyDataSetChanged()
    }

    /** Return the size of your dataset (invoked by the layout manager) */
    override fun getItemCount() = obstacles.size
}
