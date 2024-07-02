import fs from 'fs';
import path from 'path';
import {mergeContents} from '@expo/config-plugins/build/utils/generateCode';

export const writeToPodfile = (
  projectRoot: string,
  key: string,
  value: string,
) => {
  const podfilePath = path.join(projectRoot, 'ios', 'Podfile');
  const podfileContent = fs.readFileSync(podfilePath, 'utf8');

  const newPodfileContent = mergeContents({
    tag: `rn-video-set-${key.toLowerCase()}`,
    src: podfileContent,
    newSrc: `$${key} = ${value}`,
    anchor: /platform :ios/,
    offset: 0,
    comment: '#',
  });

  if (newPodfileContent.didMerge) {
    fs.writeFileSync(podfilePath, newPodfileContent.contents);
  } else {
    console.warn(`RNV - Failed to write "$${key} = ${value}" to Podfile`);
  }
};
