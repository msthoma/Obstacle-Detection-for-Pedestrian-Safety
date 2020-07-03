package cy.org.rise.obsai.ui

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import cy.org.rise.obsai.R
import cy.org.rise.obsai.db.Obstacle
import cy.org.rise.obsai.utils.TAG
import kotlinx.android.synthetic.main.row_item.view.*
import java.io.File

/**
 * Provides views to the RecyclerView with obstacle data.
 */
class CustomAdapter internal constructor() :
    RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    private var obstacles = emptyList<Obstacle>() // Cached copy of obstacles

    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val textView: TextView
        val uploadStatusView: TextView

        init {
            // Define click listener for the ViewHolder's View.
            v.setOnClickListener { Log.d(TAG(), "Element $adapterPosition clicked.") }
            textView = v.findViewById(R.id.textView)
            uploadStatusView = v.findViewById(R.id.upload_status_view)
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
        viewHolder.textView.text = "Type:\t${obs.obstacleType}\n" +
                "Location:\t${obs.location.latitude}, ${obs.location.longitude}\n" +
                "Orientation:\tx: ${"%.3f".format(obs.orientation.x)}, " +
                "y: ${"%.3f".format(obs.orientation.y)}, " +
                "z: ${"%.3f".format(obs.orientation.z)}\n" +
                "${obs.timeStamp}"

        when (obs.uploadStatus) {
            "Uploading..." -> viewHolder.uploadStatusView.text = obs.uploadStatus
            "200" -> {
                viewHolder.uploadStatusView.apply {
                    text = "Upload status: Success (${obs.uploadStatus})"
                    setTextColor(resources.getColor(R.color.colorPrimary))
                }
            }
            else -> {
                viewHolder.uploadStatusView.apply {
                    text = "Upload status: Failure (${obs.uploadStatus})"
                    setTextColor(resources.getColor(R.color.design_default_color_error))
                }
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
