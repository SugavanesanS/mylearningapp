import { StyleSheet, Text, TouchableOpacity, View, Image, Alert, Platform, NativeModules } from 'react-native'
import React from 'react'
import { useSafeAreaInsets } from 'react-native-safe-area-context';

const { CameraModule } = NativeModules;

const Gallaryscreen = () => {

    const insets = useSafeAreaInsets();

    const [image, setImage] = React.useState<string | null>(null);

    const AndroidCapturingImage = async () => {
        try {
            const data = await CameraModule.captureImage({
                'crop': false
            })
            setImage(data?.uri);
        } catch (e) {
            console.error('Error capturing photo:', e);
        }
    }

    const AndroidChooseImageFromGallary = async () => {
        try {
            const data = await CameraModule.pickImage({
                'crop': true
            });
            setImage(data?.uri);
        } catch (e) {
            console.error('Error capturing photo:', e);
        }
    }

    const handleCapture = async () => {
        if (Platform.OS === 'android') {
            AndroidCapturingImage();
        } else if (Platform.OS === 'ios') {
        }
    }

    const handleChooseFromGallery = async () => {
        if (Platform.OS === 'android') {
            AndroidChooseImageFromGallary();
        } else if (Platform.OS === 'ios') {
        }
    }

    const handlePress = async () => {
        Alert.alert(
            "Select Image",
            "Choose an option",
            [
                {
                    text: "Cancel",
                },
                {
                    text: "Choose from Gallery",
                    onPress: handleChooseFromGallery
                },
                {
                    text: "Capture Image",
                    onPress: handleCapture
                },
            ],
        )
    }

    return (
        <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center', marginTop: insets.top, marginBottom: insets.bottom }}>
            <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center', gap: 20 }}>
                <View style={{
                    width: 400, height: 400, justifyContent: 'center', alignItems: 'center',
                    borderRadius: 7, borderWidth: 0.5
                }}>
                    {image && <Image source={{ uri: image }} style={{ resizeMode: 'contain', width: '100%', height: '100%' }} />}
                </View>
                <TouchableOpacity onPress={handlePress} style={{
                    backgroundColor: 'yellow', padding: 10, borderRadius: 5, marginBottom: 20, paddingHorizontal: 20,
                    shadowColor: "#000",
                    shadowOpacity: 0.25,
                    shadowRadius: 3.84,
                    elevation: 4

                }}>
                    <Text style={{ color: 'black' }}>Upload</Text>
                </TouchableOpacity>
            </View>
        </View>


    )
}

export default Gallaryscreen

const styles = StyleSheet.create({})