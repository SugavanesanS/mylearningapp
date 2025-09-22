import { StyleSheet, Text, Touchable, TouchableOpacity, View, NativeModules, Image, Alert } from 'react-native'
import React from 'react'
import { useSafeAreaInsets } from 'react-native-safe-area-context';

const Homescreen = () => {

    const insets = useSafeAreaInsets();
    const { MyNativeModule, CameraModule } = NativeModules;


    const [image, setImage] = React.useState<string | null>(null);

    const handleCapture = async () => {
        try {
            const uri = await CameraModule.captureImage();
            setImage(uri);
            console.log('Captured image URI:', uri);
        } catch (e) {
            console.error('Error capturing photo:', e);
        }
    }

    const handleChooseFromGallery = async () => {
        try {
            const uri = await CameraModule.pickImage();
            setImage(uri);
            console.log('Captured image URI:', uri);
        } catch (e) {
            console.error('Error capturing photo:', e);
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

    const handleAlert = () => {
        MyNativeModule.showNetWorkErrorToast();
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
                <TouchableOpacity onPress={handleAlert} style={{
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

export default Homescreen

const styles = StyleSheet.create({})