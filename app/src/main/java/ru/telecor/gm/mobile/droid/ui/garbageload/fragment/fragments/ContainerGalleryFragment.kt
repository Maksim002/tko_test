package ru.telecor.gm.mobile.droid.ui.garbageload.fragment.fragments

import android.os.Build
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.fragment_container_gallery.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import ru.telecor.gm.mobile.droid.R
import ru.telecor.gm.mobile.droid.di.Scopes
import ru.telecor.gm.mobile.droid.entities.db.ProcessingPhoto
import ru.telecor.gm.mobile.droid.entities.photo.GarbagePhotoModel
import ru.telecor.gm.mobile.droid.entities.task.StatusTaskExtended
import ru.telecor.gm.mobile.droid.presentation.garbageload.ContainerGalleryPresenter
import ru.telecor.gm.mobile.droid.presentation.garbageload.ContainerGalleryView
import ru.telecor.gm.mobile.droid.ui.base.BaseBottomSheetFragment
import ru.telecor.gm.mobile.droid.ui.garbageload.rv.GalleryDetailCon
import toothpick.Toothpick

class ContainerGalleryFragment(
    var listener: ListenerCompleteRoute,
    var item: ArrayList<GarbagePhotoModel> = arrayListOf(),
    var itemList: StatusTaskExtended,
    var itemRoute: List<StatusTaskExtended>
) : BaseBottomSheetFragment(), ContainerGalleryView {

    private var map: HashMap<String, ProcessingPhoto> = hashMapOf()
    private var mapList: ArrayList<ProcessingPhoto> = arrayListOf()
    private lateinit var adapter: GalleryDetailCon

    @InjectPresenter
    lateinit var presenter: ContainerGalleryPresenter

    @ProvidePresenter
    fun providePresenter(): ContainerGalleryPresenter {
        return Toothpick.openScope(Scopes.SERVER_SCOPE)
            .getInstance(ContainerGalleryPresenter::class.java)
    }

    override val layoutRes = R.layout.fragment_container_gallery

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initClick()
        initAdapter()
    }

    private fun initAdapter() {
        adapter = GalleryDetailCon {
            listener.getDeletePhoto(it)
            mapList.remove(it)
            adapter.update(mapList)
            listSize(mapList)
        }

        resultUnloading.text = itemList.containerStatus?.volumeAct?.toString() ?: ""
        resultUnloadingConInfo.text = itemList.containerType.name ?: ""

        sortTaskId(itemList.id.toLong())
        map.map {
            mapList.add(it.value)
            adapter.update(mapList)
        }
        containerGallery.adapter = adapter
    }

    private fun sortTaskId(idInt: Long) {
        if (itemList.groupId != null) {
            itemRoute.forEach { task ->
                if (itemList.groupId.toString() == task.groupId.toString()) {
                    item.map { photo ->
                        photo.item.map {
                            if (it.conId.firstOrNull { conId -> conId == idInt } != null) map[it.id.toString()] =
                                it
                        }
                    }
                }
            }
        } else {
            item.map { photo ->
                photo.item.map {
                    if (it.conId.firstOrNull { conId -> conId == idInt } != null)
                        map[it.id.toString()] = it
                }
            }
        }
    }

    private fun initClick() {

    }

    private fun listSize(mapList: ArrayList<ProcessingPhoto>) {
        if (mapList.size == 0) this.dismiss()
    }

    override fun getTheme(): Int {
        return if (Build.VERSION.SDK_INT > 22) {
            R.style.AppBottomSheetDialogTheme
        } else {
            0
        }
    }

    interface ListenerCompleteRoute {
        fun getDeletePhoto(photo: ProcessingPhoto)
    }
}