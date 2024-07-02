import type {Component, RefObject, ComponentClass} from 'react';
import {Image, findNodeHandle, type ImageSourcePropType} from 'react-native';
import type {ReactVideoSource, ReactVideoSourceProperties} from './types/video';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function generateHeaderForNative(obj?: Record<string, any>) {
  if (!obj) {
    return [];
  }
  return Object.entries(obj).map(([key, value]) => ({key, value}));
}

type Source = ImageSourcePropType | ReactVideoSource;

export function resolveAssetSourceForVideo(
  source: Source,
): ReactVideoSourceProperties {
  // will convert source id to uri
  const convertToUri = (sourceItem: number): string | undefined => {
    const resolveItem = Image.resolveAssetSource(sourceItem);
    if (resolveItem) {
      return resolveItem.uri;
    } else {
      console.warn('cannot resolve item ', sourceItem);
      return undefined;
    }
  };

  // This is deprecated, but we need to support it for backward compatibility
  if (typeof source === 'number') {
    return {
      uri: convertToUri(source),
    };
  }

  if ('uri' in source && typeof source.uri === 'number') {
    return {
      ...source,
      uri: convertToUri(source.uri),
    };
  }

  return source as ReactVideoSourceProperties;
}

/**
 * @deprecated
 * Do not use this fn anymore. "findNodeHandle" will be deprecated.
 * */
export function getReactTag(
  ref: RefObject<
    | Component<unknown, unknown, unknown>
    | ComponentClass<unknown, unknown>
    | null
  >,
): number {
  if (!ref.current) {
    throw new Error('Video Component is not mounted');
  }

  const reactTag = findNodeHandle(ref.current);

  if (!reactTag) {
    throw new Error(
      'Cannot find reactTag for Video Component in components tree',
    );
  }

  return reactTag;
}
