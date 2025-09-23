import { NativeModules } from "react-native";

export interface CameraModuleType {
  captureImage(options?: { crop?: boolean }): Promise<{ uri: string; type: string }>;
  pickImage(options?: { crop?: boolean }): Promise<{ uri: string; type: string }>;
}

declare module "react-native" {
  interface NativeModulesStatic {a
    CameraModule: CameraModuleType;
  }
}
