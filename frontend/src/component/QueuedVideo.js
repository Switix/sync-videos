import React from 'react';

function QueuedVideo({ videoData }) {

    return (
        <div className="bg-neutral-900 rounded-lg shadow-lg overflow-hidden w-full flex">
            <img src={videoData.thumbnail} alt={videoData.title} className=" w-1/3  object-cover" />
            <div className="px-4 flex flex-col justify-around w-2/3 ">
                <h3 className="text-lg font-bold text-white line-clamp-3">{videoData.title}</h3>
                <p className="text-neutral-400 ">By {videoData.author}</p>
            </div>
        </div>
    );
}

export default QueuedVideo;