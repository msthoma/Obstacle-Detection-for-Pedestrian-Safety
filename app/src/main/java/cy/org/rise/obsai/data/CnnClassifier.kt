package cy.org.rise.obsai.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.TensorProcessor
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp
import org.tensorflow.lite.support.image.ops.Rot90Op
import org.tensorflow.lite.support.label.TensorLabel
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.MappedByteBuffer
import kotlin.math.min

class CnnClassifier(tfLiteModel: MappedByteBuffer, private val cnnLabels: MutableList<String>) {
    private val tfLiteInterpreter = Interpreter(tfLiteModel, Interpreter.Options())

    private val imageTensorIndex = 0
    private val imageShape = tfLiteInterpreter.getInputTensor(imageTensorIndex).shape()
    private val imageSizeY = imageShape[1]
    private val imageSizeX = imageShape[2]

    private val imageDataType = tfLiteInterpreter.getInputTensor(imageTensorIndex).dataType()
    private val probabilityTensorIndex = 0
    private val probabilityShape = tfLiteInterpreter.getOutputTensor(probabilityTensorIndex).shape()
    private val probabilityDataType =
        tfLiteInterpreter.getOutputTensor(probabilityTensorIndex).dataType()

    private var inputImageBuffer = TensorImage(imageDataType)

    private val outputProbabilityBuffer =
        TensorBuffer.createFixedSize(probabilityShape, probabilityDataType)

    private val probabilityProcessor =
        TensorProcessor.Builder().add(NormalizeOp(PROBABILITY_MEAN, PROBABILITY_STD))
            .build()

    fun classifyPhoto(photoPath: String): Map<String, Float> {
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        val bitmap = BitmapFactory.decodeFile(photoPath, options)

        val cropSize = min(bitmap.width, bitmap.height)

        inputImageBuffer.load(bitmap)
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeWithCropOrPadOp(cropSize, cropSize))
            .add(ResizeOp(imageSizeX, imageSizeY, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
            .add(Rot90Op(1))
            .add(NormalizeOp(IMAGE_MEAN, IMAGE_STD))
            .build()
        inputImageBuffer = imageProcessor.process(inputImageBuffer)

        // run classification
        tfLiteInterpreter.run(inputImageBuffer.buffer, outputProbabilityBuffer.buffer.rewind())

        // return map of labels and their predicted probabilities
        return TensorLabel(cnnLabels, probabilityProcessor.process(outputProbabilityBuffer))
            .mapWithFloatValue
    }

    private companion object {
        const val IMAGE_MEAN = 0.0f
        const val IMAGE_STD = 255.0f
        const val PROBABILITY_MEAN = 0.0f
        const val PROBABILITY_STD = 1.0f
    }
}
