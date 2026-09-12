package mx.tec.sabores.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mx.tec.sabores.data.RestaurantRepository
import mx.tec.sabores.domain.RatingSummary
import mx.tec.sabores.domain.Restaurant
import mx.tec.sabores.domain.RestaurantEnLista
import mx.tec.sabores.domain.Review
import retrofit2.HttpException
import java.io.IOException

data class MyReviewItem(val restaurantName: String, val review: Review)

/** El restaurante y sus reseñas, que la pantalla de detalle necesita juntos. */
data class Detalle(
    val restaurant: Restaurant,
    val reviews: List<Review>
) {
    val summary: RatingSummary = RatingSummary.from(reviews)
}

class SaboresViewModel(
    private val repository: RestaurantRepository = RestaurantRepository()
) : ViewModel() {

    var restaurantes by mutableStateOf<UiState<List<RestaurantEnLista>>>(UiState.Cargando)
        private set

    var detalle by mutableStateOf<UiState<Detalle>>(UiState.Cargando)
        private set

    var mias by mutableStateOf<UiState<List<MyReviewItem>>>(UiState.Cargando)
        private set

    var mensajeAcciones by mutableStateOf<String?>(null)
        private set

    fun limpiarMensaje() {
        mensajeAcciones = null
    }

    fun cargarMisResenas() {
        viewModelScope.launch {
            mias = UiState.Cargando
            mias = pedir {
                val listaRestaurantes: List<RestaurantEnLista> = when (val estado = restaurantes) {
                    is UiState.Exito -> estado.datos
                    else -> emptyList()
                }
                val reviews = repository.getMyReviews()
                reviews.map { review ->
                    MyReviewItem(
                        restaurantName = listaRestaurantes.find { it.restaurant.id == review.restaurantId }?.restaurant?.name
                            ?: "Restaurante",
                        review = review
                    )
                }
            }
        }
    }

    private suspend fun <T> pedir(block: suspend () -> T): UiState<T> = try {
        UiState.Exito(block())
    } catch (e: IOException) {
        UiState.Error("No hay conexión. Revisa tu internet.")
    } catch (e: HttpException) {
        UiState.Error(mensajeDe(e))
    }

    fun cargarRestaurantes() {
        viewModelScope.launch {
            restaurantes = UiState.Cargando
            restaurantes = pedir { repository.getAllForList() }
        }
    }

    fun cargarDetalle(id: Int) {
        viewModelScope.launch {
            detalle = UiState.Cargando
            detalle = pedir { Detalle(repository.getById(id), repository.getReviews(id)) }
        }
    }

    fun borrar(review: Review) {
        viewModelScope.launch {
            try {
                val seBorro = repository.deleteReview(review.id)
                if (seBorro) {
                    cargarMisResenas()
                } else {
                    mensajeAcciones = "Esta reseña no es tuya."
                }
            } catch (e: IOException) {
                mensajeAcciones = "No hay conexión. No se pudo borrar."
            } catch (e: HttpException) {
                mensajeAcciones = mensajeDe(e)
            }
        }
    }

    fun editar(id: Int, stars: Int, comment: String) {
        viewModelScope.launch {
            try {
                repository.editReview(id, stars, comment)
                cargarMisResenas()
            } catch (e: IOException) {
                mensajeAcciones = "No hay conexión. No se pudo editar."
            } catch (e: HttpException) {
                mensajeAcciones = mensajeDe(e)
            }
        }
    }
}