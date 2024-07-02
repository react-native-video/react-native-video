import type {TurboModule} from 'react-native';
import {TurboModuleRegistry} from 'react-native';
import type {Int32} from 'react-native/Libraries/Types/CodegenTypes';

interface Spec extends TurboModule {
  getWidevineLevel: () => Promise<Int32>;
  isCodecSupported: (
    mimeType: string,
    width: Int32,
    height: Int32,
  ) => Promise<'unsupported' | 'hardware' | 'software'>;
  isHEVCSupported: () => Promise<'unsupported' | 'hardware' | 'software'>;
}

export default TurboModuleRegistry.getEnforcing<Spec>(
  'RNVDecoderPropertiesModule',
);
