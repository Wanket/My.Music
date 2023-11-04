package ru.tigrilla.my_music.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import ru.tigrilla.my_music.databinding.InfoFragmentBinding
import ru.tigrilla.my_music.view_model.InfoViewModel

@AndroidEntryPoint
class InfoFragment: Fragment() {
    private val args: InfoFragmentArgs by navArgs()

    private val viewModel by viewModels<InfoViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val bindings = InfoFragmentBinding.inflate(inflater, container, false).apply {
            viewModel = this@InfoFragment.viewModel
        }

        viewModel.track = args.track

        return bindings.root
    }
}
