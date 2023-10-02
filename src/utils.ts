import type { Component, RefObject, ComponentClass } from 'react';
import { Image, UIManager, findNodeHandle } from "react-native";
import type { ImageSourcePropType } from 'react-native';
import type { ReactVideoSource } from './types/video';

type Source = ImageSourcePropType | ReactVideoSource;

export function resolveAssetSourceForVideo(source: Source): ReactVideoSource {
  if (typeof source === 'number') {
    return {
      uri: Image.resolveAssetSource(source).uri,
    };
  }
  return source as ReactVideoSource;
}

export function getReactTag(ref: RefObject<Component<any, any, any> | ComponentClass<any, any> | null>): number {
  if (!ref.current) {
    throw new Error("Video Component is not mounted");
  }

  const reactTag = findNodeHandle(ref.current);

  if (!reactTag) {
    throw new Error("Cannot find reactTag for Video Component in components tree");
  }

  return reactTag;
}

export function getViewManagerConfig(name: string) {
  if('getViewManagerConfig' in UIManager) {
    return UIManager.getViewManagerConfig(name);
  }

  return UIManager[name];
}