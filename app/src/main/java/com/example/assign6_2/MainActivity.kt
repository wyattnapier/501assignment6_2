package com.example.assign6_2

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.assign6_2.ui.theme.Assign6_2Theme
import kotlin.math.atan2
import kotlin.math.sqrt

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager

    // Sensors
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null
    private var gyroscope: Sensor? = null

    // Raw sensor values
    private val accelValues = FloatArray(3)
    private val magnetValues = FloatArray(3)

    // UI State
    private var _heading by mutableFloatStateOf(0f)
    private var _pitch by mutableFloatStateOf(0f)
    private var _roll by mutableFloatStateOf(0f)

    override fun onCreate(savedInstanceState: Bundle?) {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Assign6_2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CompassAndLevelScreen(
                        heading = _heading,
                        pitch = _pitch,
                        roll = _roll
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.also { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        magnetometer?.also { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        gyroscope?.also { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                System.arraycopy(event.values, 0, accelValues, 0, event.values.size)
            }

            Sensor.TYPE_MAGNETIC_FIELD -> {
                System.arraycopy(event.values, 0, magnetValues, 0, event.values.size)
            }

            Sensor.TYPE_GYROSCOPE -> {
                _pitch += event.values[1] * 2f
                _roll += event.values[0] * 2f
            }
        }

        // Compute compass heading
        val rotationMatrix = FloatArray(9)
        val orientation = FloatArray(3)

        val success = SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelValues,
            magnetValues
        )

        if (success) {
            SensorManager.getOrientation(rotationMatrix, orientation)
            val azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
            _heading = (azimuth + 360) % 360
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

// --------------------------------------------------
// UI
// --------------------------------------------------

@Composable
fun CompassAndLevelScreen(heading: Float, pitch: Float, roll: Float) {

    val animatedHeading by animateFloatAsState(targetValue = heading)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(20, 20, 30))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {

        // ---------------- COMPASS -----------------
        Text(
            text = "Compass",
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        Box(
            modifier = Modifier
                .size(220.dp)
                .background(Color(50, 50, 70), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(200.dp)) {
                rotate(-animatedHeading) {
                    drawLine(
                        Color.Red,
                        start = center,
                        end = center.copy(y = 20f),
                        strokeWidth = 10f
                    )
                }
            }
        }

        Text(
            text = "Heading: ${heading.toInt()}°",
            color = Color.White
        )

        Spacer(modifier = Modifier.height(40.dp))

        // ---------------- LEVEL -----------------
        Text(
            text = "Digital Level",
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        Box(
            modifier = Modifier
                .size(180.dp)
                .background(Color(70, 40, 40), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .background(Color(200, 200, 80), CircleShape)
                    .padding(10.dp)
                    .rotate(roll)
            ) {
            }
        }

        Text(text = "Pitch: ${pitch.toInt()}°", color = Color.White)
        Text(text = "Roll : ${roll.toInt()}°", color = Color.White)
    }
}