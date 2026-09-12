package mx.tec.sabores.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mx.tec.sabores.domain.Review
import mx.tec.sabores.domain.ReviewValidator
import mx.tec.sabores.ui.components.StarPicker
import mx.tec.sabores.ui.components.StarsRow
import mx.tec.sabores.ui.state.MyReviewItem

@Composable
fun MyReviewsScreen(
    items: List<MyReviewItem>,
    mensajeAcciones: String?,
    onLimpiarMensaje: () -> Unit,
    onBorrar: (Review) -> Unit,
    onEditar: (id: Int, stars: Int, comment: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var reviewEnEdicion by remember { mutableStateOf<Review?>(null) }

    if (items.isEmpty()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Todavía no has reseñado ningún lugar.",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items) { item ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(item.restaurantName, style = MaterialTheme.typography.titleMedium)
                    StarsRow(item.review.stars)
                    Spacer(Modifier.height(6.dp))
                    Text(item.review.comment, style = MaterialTheme.typography.bodyMedium)

                    Row {
                        Button(onClick = { onBorrar(item.review) }) { Text("Borrar") }
                        Button(onClick = { reviewEnEdicion = item.review }) { Text("Editar") }
                    }
                }
            }
        }
    }

    reviewEnEdicion?.let { review ->
        var stars by remember(review) { mutableStateOf(review.stars) }
        var comment by remember(review) { mutableStateOf(review.comment) }
        val comentarioValido = ReviewValidator.validateComment(comment) == null

        AlertDialog(
            onDismissRequest = { reviewEnEdicion = null },
            title = { Text("Editar reseña") },
            text = {
                Column {
                    StarPicker(value = stars, onValueChange = { stars = it })
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { if (it.length <= ReviewValidator.COMMENT_MAX) comment = it },
                        label = { Text("Comentario") },
                        isError = !comentarioValido
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = comentarioValido,
                    onClick = {
                        onEditar(review.id, stars, comment)
                        reviewEnEdicion = null
                    }
                ) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { reviewEnEdicion = null }) { Text("Cancelar") }
            }
        )
    }

    mensajeAcciones?.let { mensaje ->
        Text(
            text = mensaje,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}