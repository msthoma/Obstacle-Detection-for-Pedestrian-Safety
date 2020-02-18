package cy.org.rise.obsai

import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_obstacle_list.*
import java.io.File

class ObstacleListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var dataset: Array<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_obstacle_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        toolbar.title = "Obstacles"
        // TODO set toolbar title here
        recyclerView = recycler_view
        recyclerView.layoutManager = LinearLayoutManager(context)
        initDataset()
        val adapter = CustomAdapter(dataset)
        recyclerView.adapter = adapter

        fab.setOnClickListener {
            view.findNavController().navigate(R.id.cameraFragment)
        }
    }

    private fun initDataset() {
        val path = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath
        dataset = File(path).list()
    }
}