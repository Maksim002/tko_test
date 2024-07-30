package ru.telecor.gm.mobile.droid.presentation.garbageload

import kotlinx.coroutines.launch
import ru.telecor.gm.mobile.droid.entities.db.ProcessingPhoto
import ru.telecor.gm.mobile.droid.entities.photo.GarbagePhotoModel
import ru.telecor.gm.mobile.droid.model.data.storage.GmServerPrefs
import ru.telecor.gm.mobile.droid.model.data.storage.SettingsPrefs
import ru.telecor.gm.mobile.droid.model.interactors.PhotoInteractor
import ru.telecor.gm.mobile.droid.model.interactors.RouteInteractor
import ru.telecor.gm.mobile.droid.model.system.IResourceManager
import ru.telecor.gm.mobile.droid.presentation.base.BasePresenter
import java.util.ArrayList
import javax.inject.Inject

class ContainerGalleryPresenter  @Inject constructor(
    private val routeInteractor: RouteInteractor,
    private val gmServerPrefs: GmServerPrefs,
    private val rm: IResourceManager,
    val settingsPrefs: SettingsPrefs,
    private val photoInteractor: PhotoInteractor
) : BasePresenter<ContainerGalleryView>() {

}
